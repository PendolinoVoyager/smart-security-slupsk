"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Loader2 } from "lucide-react";

type LoginFormProps = {
  onLoginSuccess?: () => void;
};

export default function LoginForm({ onLoginSuccess }: LoginFormProps) {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    // Replace with real login logic
    const result = new Error("implement auth");
    setIsLoading(false);

    if (result instanceof Error) {
      setError(result.message);
      return;
    }

    onLoginSuccess && onLoginSuccess();
    router.push("/");
  };

  return (
    <Card className="w-[350px] p-6">
      <CardContent>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <h2 className="text-2xl font-semibold text-center">Login</h2>
          <p className="text-sm text-muted-foreground text-center mb-2">
            Default user is admin@example.com with password Password.123
          </p>

          <div className="flex flex-col gap-2">
            <Label htmlFor="email">Email Address</Label>
            <Input
              id="email"
              type="email"
              required
              autoComplete="email"
              autoFocus
              value={email}
              onChange={(e: any) => setEmail(e?.target?.value || "error")}
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              required
              autoComplete="current-password"
              value={password}
              onChange={(e: any) => setPassword(e.target.value)}
            />
          </div>

          {error && (
            <Alert variant="destructive">
              <AlertTitle>Error</AlertTitle>
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <Button type="submit" disabled={isLoading}>
            {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Login
          </Button>

          <div className="text-center mt-2 space-y-1 text-sm">
            <p>
              <span
                className="text-blue-600 hover:underline cursor-pointer"
                onClick={() => router.push("/reset-password")}
              >
                Forgotten Password?
              </span>
            </p>
            <p>
              Don’t have an account?{" "}
              <span
                className="text-blue-600 hover:underline cursor-pointer"
                onClick={() => router.push("/register")}
              >
                Create one now
              </span>
            </p>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
