import { getStreams } from "@/api/stream";
import { getAuthData } from "@/lib/auth/server";
import { Metadata } from "next";
import { redirect } from "next/navigation";

export const metadata: Metadata = {
  title: "Smart Security | View camera feed",
};

export default async function PreviewStream() {
  const authData = await getAuthData();
  if (!authData) {
    redirect("/");
  }
  const streams = await getStreams(authData.token);
  console.log(streams);
  return <h1>stream :)</h1>;
}
