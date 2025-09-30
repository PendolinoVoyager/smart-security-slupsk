import ActivationForm from "@/components/auth/activationForm";
import { Card, CardContent } from "@/components/ui/card";
import { getAuthData } from "@/lib/auth/client";
import { cn } from "@/lib/utils"; // if you have a cn() util, optional

export default async function ActivateAccountPage() {
  const authData = await getAuthData();

  return (
    <div className="flex h-screen flex-col md:flex-row justify-center items-center md:items-baseline ">
      <InformativePanel />

      <div className="flex flex-1 items-center justify-center bg-secondary">
        <Card className="w-full max-w-xs p-6">
          <CardContent className="flex flex-col items-center">
            <h2 className="text-2xl font-bold mb-6">Activate Your Account</h2>
            <ActivationForm />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function InformativePanel() {
  return (
    <div className="flex flex-1 flex-col items-center pt-24 text-primary bg-gradient-to-br from-primary-700 to-primary-500 text-center">
      <h1 className="text-4xl font-bold">Activate your account! 👏</h1>
      <p className="mt-6 text-center max-w-md text-lg">
        To complete your registration, activate your account using the form on
        the right. Enter your email and the activation token you received via
        email.
      </p>
    </div>
  );
}
