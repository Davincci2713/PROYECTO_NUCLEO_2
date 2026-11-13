"use client";

import { MarcoBackoffice } from "@/components/MarcoBackoffice";
import type { ReactNode } from "react";

export default function LayoutBackoffice({ children }: { children: ReactNode }) {
  return <MarcoBackoffice>{children}</MarcoBackoffice>;
}
