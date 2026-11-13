package mx.gob.gld.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class Ruta implements RequestMatcher {

    private final String metodo;
    private final Pattern patron;

    public Ruta(String metodo, String ant) {
        this.metodo = metodo;
        String regex = ant
                .replace(".", "\\.")
                .replace("**", "<<<>>>")
                .replace("*", "[^/]*")
                .replace("<<<>>>", ".*");
        this.patron = Pattern.compile(regex);
    }

    public static RequestMatcher alguna(RequestMatcher... matchers) {
        List<RequestMatcher> lista = Arrays.asList(matchers);
        return request -> {
            for (RequestMatcher matcher : lista) {
                if (matcher.matches(request)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static RequestMatcher ninguna() {
        return request -> false;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        if (metodo != null && !metodo.equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        String context = request.getContextPath() == null ? "" : request.getContextPath();
        if (!context.isEmpty() && uri.startsWith(context)) {
            uri = uri.substring(context.length());
        }
        return patron.matcher(uri).matches();
    }
}
