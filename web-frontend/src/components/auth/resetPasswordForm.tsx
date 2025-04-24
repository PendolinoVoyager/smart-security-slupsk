"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { resetPasswordAction } from "@/lib/auth/server"; // adjust the import if needed
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Loader2 } from "lucide-react"; // icon for loading spinner
import { Label } from "../ui/label";

export default function ResetPasswordForm() {
  const router = useRouter();

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    const formData = new FormData(e.currentTarget);
    const email = formData.get("email") as string;

    if (!email) {
      setError("Please provide both email and activation token.");
      return;
    }

    setIsLoading(true);

    const result = await resetPasswordAction(null, formData);
    if (result instanceof Error) {
      setError(result.message);
    } else {
      router.push("/auth/password-reset-confirmation");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="w-full flex flex-col gap-4">
      <Label htmlFor="email">Please enter your email</Label>
      <Input
        id="email"
        name="email"
        type="email"
        placeholder="Email Address"
        required
      />

      {error && (
        <Alert variant="destructive">
          <AlertTitle>Error</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <Button type="submit" className="mt-2" disabled={isLoading}>
        {isLoading ? (
          <Loader2 className="h-4 w-4 animate-spin" />
        ) : (
          "Send password reset email"
        )}
      </Button>
    </form>
  );
}
