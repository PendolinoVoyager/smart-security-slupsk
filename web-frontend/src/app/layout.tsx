import { ReactNode } from "react";
import "@/app/globals.css";
import {
  NavigationMenu,
  NavigationMenuList,
  NavigationMenuItem,
  NavigationMenuLink,
} from "@/components/ui/navigation-menu";
import { cookies } from "next/headers";
import MainNav from "@/components/nav/mainNav";
import { getAuthData } from "@/lib/auth/server";
export const metadata = {
  title: "Smart Security Slupsk",
  description: "Smart Home Secuirty",
};

export default async function RootLayout({
  children,
}: {
  children: ReactNode;
}) {
  const isLoggedIn = !!(await getAuthData());
  return (
    <html lang="en">
      <body className="bg-gray-100 text-gray-900">
        <header className="bg-white border-b p-4">
          <MainNav isLoggedIn={isLoggedIn} />
        </header>
        <main className="container mx-auto p-6">{children}</main>
      </body>
    </html>
  );
}
