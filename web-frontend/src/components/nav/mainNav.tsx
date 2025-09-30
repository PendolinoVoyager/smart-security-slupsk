import {
  NavigationMenu,
  NavigationMenuList,
  NavigationMenuItem,
  NavigationMenuLink,
} from "@/components/ui/navigation-menu";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils"; // shadcn's cn utility for className merging
import LoginButton from "../auth/loginButton";
import { logoutAction } from "@/lib/auth/server";

export default function Navbar({ isLoggedIn }: { isLoggedIn: boolean }) {
  return (
    <nav className="container mx-auto my-large">
      <NavigationMenu>
        <NavigationMenuList>
          <NavigationMenuItem>
            <NavigationMenuLink className="px-4 py-2" href="/">
              Home
            </NavigationMenuLink>
          </NavigationMenuItem>

          {!isLoggedIn ? (
            <>
              <NavigationMenuItem>
                <NavigationMenuLink className="px-4 py-2">
                  <LoginButton />
                </NavigationMenuLink>
              </NavigationMenuItem>
              <NavigationMenuItem>
                <NavigationMenuLink className="px-4 py-2" href="/auth/register">
                  Sign Up
                </NavigationMenuLink>
              </NavigationMenuItem>
            </>
          ) : (
            <>
              <NavigationMenuItem>
                <NavigationMenuLink
                  className="px-4 py-2 hover:bg-accent hover:text-accent-foreground rounded-md"
                  href="/devices"
                >
                  Devices
                </NavigationMenuLink>
              </NavigationMenuItem>
              <NavigationMenuItem>
                <form action={logoutAction}>
                  <Button
                    type="submit"
                    variant="ghost"
                    className="w-full px-4 py-2 hover:bg-accent hover:text-accent-foreground rounded-md"
                    style={{ cursor: "pointer" }}
                  >
                    Logout
                  </Button>
                </form>
              </NavigationMenuItem>
            </>
          )}
        </NavigationMenuList>
      </NavigationMenu>
    </nav>
  );
}
