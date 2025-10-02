import HomePagePromo from "./homePagePromo";
import { getAuthData } from "@/lib/auth/server";

export default async function Home() {
  const loggedIn = !!(await getAuthData());

  if (loggedIn) {
    return <h1>Dashboard</h1>;
  } else return <HomePagePromo />;
}
