"use client";

import { useActionState, useState } from "react";
import { Loader2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { useRouter } from "next/navigation";
import { registerAction } from "@/lib/auth/server";

const RegistrationForm = () => {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    const formData = new FormData(e.currentTarget);
    const result = await registerAction(null, formData);

    setIsLoading(false);

    if (!result.success) {
      setError(result.message);
    } else {
      router.push("/auth/activate");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 w-full">
      <div className="grid w-full items-center gap-1.5">
        <Label htmlFor="name">First Name</Label>
        <Input id="name" name="name" required />
      </div>

      <div className="grid w-full items-center gap-1.5">
        <Label htmlFor="last_name">Last Name</Label>
        <Input id="last_name" name="last_name" required />
      </div>

      <div className="grid w-full items-center gap-1.5">
        <Label htmlFor="email">Email Address</Label>
        <Input
          id="email"
          name="email"
          type="email"
          autoComplete="email"
          required
        />
      </div>

      <div className="grid w-full items-center gap-1.5">
        <Label htmlFor="password">Password</Label>
        <Input
          id="password"
          name="password"
          type="password"
          autoComplete="new-password"
          required
        />
      </div>

      <div className="grid w-full items-center gap-1.5">
        <Label htmlFor="confirmPassword">Confirm Password</Label>
        <Input
          id="confirmPassword"
          name="confirmPassword"
          type="password"
          autoComplete="new-password"
          required
        />
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertTitle>Error</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <Button type="submit" className="w-full mt-4" disabled={isLoading}>
        {isLoading ? (
          <Loader2 className="h-4 w-4 animate-spin" />
        ) : (
          "Register Now"
        )}
      </Button>
    </form>
  );
};

export default RegistrationForm;
