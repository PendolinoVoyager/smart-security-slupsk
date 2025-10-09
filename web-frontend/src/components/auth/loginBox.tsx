"use client";

import { useActionState } from "react";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

import { loginAction } from "@/lib/auth/server";
import Link from "next/link";
import { redirect } from "next/navigation";

const initialState = { success: false, message: "" };

export default function LoginForm() {
  const [state, formAction] = useActionState(loginAction, initialState);
  if (!!state.success) {
    redirect("/");
  }
  return (
    <CardContent>
      <form action={formAction} className="flex flex-col gap-4">
        <h2 className="text-2xl font-semibold text-center">Login</h2>
        <p className="text-sm text-muted-foreground text-center mb-2">
          Default user is admin@example.com with password Password.123
        </p>

        <div className="flex flex-col gap-2">
          <Label htmlFor="email">Email Address</Label>
          <Input
            id="email"
            name="email"
            type="email"
            required
            autoComplete="email"
            autoFocus
          />
        </div>

        <div className="flex flex-col gap-2">
          <Label htmlFor="password">Password</Label>
          <Input
            id="password"
            type="password"
            name="password"
            required
            autoComplete="current-password"
          />
        </div>

        {!state?.success && state?.message && (
          <Alert variant="destructive">
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{state.message}</AlertDescription>
          </Alert>
        )}

        <Button type="submit">Login</Button>

        <div className="text-center mt-2 space-y-1 text-sm">
          <p>
            <Link
              className="text-blue-600 hover:underline cursor-pointer"
              href="/auth/reset-password"
            >
              Forgotten Password?
            </Link>
          </p>
          <p>
            Don’t have an account?{" "}
            <Link
              className="text-blue-600 hover:underline cursor-pointer"
              href="/auth/register"
            >
              Create one now
            </Link>
          </p>
        </div>
      </form>
    </CardContent>
  );
}
