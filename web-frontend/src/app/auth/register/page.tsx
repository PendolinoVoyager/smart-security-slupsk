import { getAuthData } from "@/lib/auth/server";
import { redirect } from "next/navigation";
// import { useRouter } from "next/router"; // not needed here
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { motion } from "motion/react";
import RegistrationForm from "@/components/auth/registrationForm";
import { Metadata } from "next";
import RegistrationInfoPanel from "@/components/auth/registrationInfoPanel";
// import InformativePanel from "../register/InformativePanel";

export const metadata: Metadata = {
  title: "Smart Security | Create your account",
};
export default async function RegisterPage() {
  const authData = await getAuthData();
  if (authData) {
    redirect("/");
  }

  return (
    <div className="flex flex-col md:flex-row bg-primary-light min-h-screen items-center md:items-stretch shadow-2xl">
      <div className="flex flex-col justify-center items-center w-full md:w-1/2 p-10 text-primary-dark">
        <RegistrationInfoPanel />
      </div>

      <div className="flex flex-1 flex-col items-center justify-center bg-secondary p-6">
        <Card className="w-full max-w-sm p-8 shadow-2xl rounded-2xl bg-white/80 backdrop-blur-md border border-white/30">
          <div className="flex flex-col items-center">
            <h2 className="text-3xl font-bold mb-6 text-primary-dark">
              Create Your Account
            </h2>
            <RegistrationForm />
          </div>
        </Card>
      </div>
    </div>
  );
}
