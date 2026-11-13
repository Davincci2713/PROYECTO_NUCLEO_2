"use client";

import { MarcoApp } from "@/components/MarcoApp";
import type { ReactNode } from "react";

export default function LayoutApp({ children }: { children: ReactNode }) {
  return <MarcoApp>{children}</MarcoApp>;
}
