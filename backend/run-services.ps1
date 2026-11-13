# Arranca el portal de servicios GLD.
# Instala el reactor si gld-common no está en el repositorio local de Maven
# y lanza cada microservicio con -am (también construye los módulos hermanos).
# Si un puerto ya está en escucha, ese proceso se reutiliza.
# El arranque no abre consola: CreateProcess usa CREATE_NO_WINDOW (0x08000000)
# sobre mvn.cmd y escribe stdout/stderr en backend/logs/<modulo>.log.

$ErrorActionPreference = "Stop"
$BackendRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $BackendRoot

$commonJar = Join-Path $env:USERPROFILE ".m2\repository\mx\gob\gld\gld-common\0.1.0\gld-common-0.1.0.jar"
if (-not (Test-Path $commonJar)) {
    Write-Host "gld-common 0.1.0 no está en el repositorio local. Ejecutando mvn -DskipTests install..."
    & mvn -DskipTests install
    if ($LASTEXITCODE -ne 0) {
        Write-Error "mvn -DskipTests install terminó con código $LASTEXITCODE"
        exit $LASTEXITCODE
    }
} else {
    Write-Host "gld-common ya está en el repositorio local."
}

$services = @(
    @{ Module = "gld-gateway";        Port = 8080 },
    @{ Module = "gld-identity";       Port = 8081 },
    @{ Module = "gld-catastro";       Port = 8082 },
    @{ Module = "gld-pagos";          Port = 8083 },
    @{ Module = "gld-tramites";       Port = 8084 },
    @{ Module = "gld-documentos";     Port = 8085 },
    @{ Module = "gld-ciudad-digital"; Port = 8086 },
    @{ Module = "gld-incidencias";    Port = 8087 }
)

$logsDir = Join-Path $BackendRoot "logs"
New-Item -ItemType Directory -Force -Path $logsDir | Out-Null

if (-not ("GldNoWindow" -as [type])) {
    Add-Type @"
using System;
using System.ComponentModel;
using System.Runtime.InteropServices;
using System.Text;

public static class GldNoWindow {
    const uint CREATE_NO_WINDOW = 0x08000000;
    const uint STARTF_USESTDHANDLES = 0x00000100;
    const uint STARTF_USESHOWWINDOW = 0x00000001;
    const uint GENERIC_READ = 0x80000000;
    const uint GENERIC_WRITE = 0x40000000;
    const uint FILE_SHARE_READ = 0x00000001;
    const uint FILE_SHARE_WRITE = 0x00000002;
    const uint OPEN_EXISTING = 3;
    const uint OPEN_ALWAYS = 4;
    const uint FILE_ATTRIBUTE_NORMAL = 0x80;

    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
    struct STARTUPINFO {
        public int cb;
        public string lpReserved;
        public string lpDesktop;
        public string lpTitle;
        public int dwX, dwY, dwXSize, dwYSize, dwXCountChars, dwYCountChars, dwFillAttribute, dwFlags;
        public short wShowWindow, cbReserved2;
        public IntPtr lpReserved2, hStdInput, hStdOutput, hStdError;
    }

    [StructLayout(LayoutKind.Sequential)]
    struct PROCESS_INFORMATION {
        public IntPtr hProcess, hThread;
        public int dwProcessId, dwThreadId;
    }

    [StructLayout(LayoutKind.Sequential)]
    struct SECURITY_ATTRIBUTES {
        public int nLength;
        public IntPtr lpSecurityDescriptor;
        public bool bInheritHandle;
    }

    [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
    static extern bool CreateProcess(
        string lpApplicationName, StringBuilder lpCommandLine,
        IntPtr lpProcessAttributes, IntPtr lpThreadAttributes, bool bInheritHandles,
        uint dwCreationFlags, IntPtr lpEnvironment, string lpCurrentDirectory,
        ref STARTUPINFO lpStartupInfo, out PROCESS_INFORMATION lpProcessInformation);

    [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
    static extern IntPtr CreateFile(
        string lpFileName, uint dwDesiredAccess, uint dwShareMode,
        ref SECURITY_ATTRIBUTES lpSecurityAttributes, uint dwCreationDisposition,
        uint dwFlagsAndAttributes, IntPtr hTemplateFile);

    [DllImport("kernel32.dll", SetLastError = true)]
    static extern bool CloseHandle(IntPtr hObject);

    public static int StartMvn(string mvnCmd, string arguments, string workDir, string logPath) {
        var sa = new SECURITY_ATTRIBUTES();
        sa.nLength = Marshal.SizeOf(typeof(SECURITY_ATTRIBUTES));
        sa.bInheritHandle = true;

        IntPtr log = CreateFile(logPath, GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, ref sa, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, IntPtr.Zero);
        if (log == new IntPtr(-1)) throw new Win32Exception(Marshal.GetLastWin32Error(), "No se pudo abrir el log");
        IntPtr nul = CreateFile("NUL", GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, ref sa, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, IntPtr.Zero);
        if (nul == new IntPtr(-1)) {
            int err = Marshal.GetLastWin32Error();
            CloseHandle(log);
            throw new Win32Exception(err, "No se pudo abrir NUL");
        }

        var si = new STARTUPINFO();
        si.cb = Marshal.SizeOf(typeof(STARTUPINFO));
        si.dwFlags = (int)(STARTF_USESTDHANDLES | STARTF_USESHOWWINDOW);
        si.wShowWindow = 0;
        si.hStdInput = nul;
        si.hStdOutput = log;
        si.hStdError = log;

        // mvn.cmd no es un EXE. El host queda sin consola (CREATE_NO_WINDOW): no hay ventana negra.
        string host = System.IO.Path.Combine(Environment.SystemDirectory, "cmd.exe");
        var command = new StringBuilder();
        command.Append('"').Append(host).Append("\" /d /c \"");
        command.Append('"').Append(mvnCmd).Append("\" ").Append(arguments).Append('"');

        PROCESS_INFORMATION pi;
        bool ok = CreateProcess(host, command, IntPtr.Zero, IntPtr.Zero, true, CREATE_NO_WINDOW, IntPtr.Zero, workDir, ref si, out pi);
        int error = Marshal.GetLastWin32Error();
        CloseHandle(log);
        CloseHandle(nul);
        if (!ok) throw new Win32Exception(error, "CreateProcess de mvn.cmd falló");
        int pid = pi.dwProcessId;
        CloseHandle(pi.hThread);
        CloseHandle(pi.hProcess);
        return pid;
    }
}
"@
}

$mvnCmd = (Get-Command mvn.cmd -ErrorAction Stop).Source

foreach ($svc in $services) {
    $busy = @(Get-NetTCPConnection -LocalPort $svc.Port -State Listen -ErrorAction SilentlyContinue)
    if ($busy.Count -gt 0) {
        $pidEnUso = $busy[0].OwningProcess
        Write-Host "$($svc.Module) ya escucha en el puerto $($svc.Port) (PID $pidEnUso). Se reutiliza."
        continue
    }

    $log = Join-Path $logsDir "$($svc.Module).log"
    if (Test-Path $log) { Remove-Item $log -Force }
    Write-Host "Arrancando $($svc.Module) en el puerto $($svc.Port) sin consola (mvn -pl $($svc.Module) -am spring-boot:run)..."
    $pidNuevo = [GldNoWindow]::StartMvn($mvnCmd, "-pl $($svc.Module) -am spring-boot:run", $BackendRoot, $log)
    Write-Host "$($svc.Module) PID $pidNuevo (CREATE_NO_WINDOW). Log: $log"
    Start-Sleep -Seconds 4
}

Write-Host "Listo. Logs en $logsDir. Los puertos ocupados no se reiniciaron. No se abrió ninguna consola."
