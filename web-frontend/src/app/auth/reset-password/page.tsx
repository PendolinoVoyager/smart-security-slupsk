import { Metadata } from "next";
import { Card, CardContent } from "@/components/ui/card";
import ResetPasswordForm from "@/components/auth/resetPasswordForm";

export const metadata: Metadata = {
  title: "Smart Security | Reset password",
};

export default function ResetPasswordPage() {
  return (
    <div className="flex h-screen flex-col md:flex-row justify-center items-center md:items-baseline ">
      <InformativePanel />

      <div className="flex flex-1 items-center justify-center">
        <Card className="w-full max-w-xs p-6">
          <CardContent className="flex flex-col items-center">
            <h2 className="text-2xl font-bold mb-6">Reset Password</h2>
            <ResetPasswordForm />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function InformativePanel() {
  return (
    <div className="flex flex-1 flex-col  items-center pt-24 bg-gradient-to-br from-primary-700 to-primary-500">
      <h1 className="text-4xl font-bold text-center">Reset your password 🔒</h1>
      <p className="mt-6 text-center max-w-md text-lg">
        To regain access to your account, reset your password using the form on
        the right. Enter your email and the reset token you received via email.
      </p>
    </div>
  );
}
