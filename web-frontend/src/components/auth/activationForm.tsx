"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { activateAccountAction } from "@/lib/auth/server"; // adjust the import if needed
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Loader2 } from "lucide-react"; // icon for loading spinner

export default function ActivationForm() {
  const router = useRouter();

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    const formData = new FormData(e.currentTarget);
    const [email, activationToken] = [
      formData.get("email") as string,
      formData.get("activationToken") as string,
    ];

    if (!email || !activationToken) {
      setError("Please provide both email and activation token.");
      return;
    }

    setIsLoading(true);

    try {
      const result = await activateAccountAction(null, formData);
      if (result instanceof Error) {
        setError(result.message);
      } else {
        router.push("/");
      }
    } catch (err: any) {
      setError(err.message || "Something went wrong.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="w-full flex flex-col gap-4">
      <Input
        id="email"
        name="email"
        type="email"
        placeholder="Email Address"
        required
      />
      <Input
        id="activationToken"
        name="activationToken"
        type="text"
        placeholder="Activation Token"
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
          "Activate Account"
        )}
      </Button>
    </form>
  );
}
