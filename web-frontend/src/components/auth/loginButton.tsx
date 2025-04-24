"use client";
import { ButtonHTMLAttributes, ClassAttributes, JSX, useState } from "react";
import {
  Popover,
  PopoverTrigger,
  PopoverContent,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import LoginBox from "./loginBox";
import { PersonIcon } from "@radix-ui/react-icons";
import { VariantProps } from "class-variance-authority";
import { ClassProp } from "class-variance-authority/types";

export default function LoginButton(
  props: JSX.IntrinsicAttributes &
    ClassAttributes<HTMLButtonElement> &
    ButtonHTMLAttributes<HTMLButtonElement> &
    VariantProps<
      (
        props?:
          | ({
              variant?:
                | "link"
                | "default"
                | "destructive"
                | "outline"
                | "secondary"
                | "ghost"
                | null
                | undefined;
              size?: "default" | "sm" | "lg" | "icon" | null | undefined;
            } & ClassProp)
          | undefined
      ) => string
    > & { asChild?: boolean }
) {
  const [open, setOpen] = useState(false);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          size="icon"
          variant="ghost"
          style={{ cursor: "pointer" }}
          {...props}
        >
          <PersonIcon /> Login
        </Button>
      </PopoverTrigger>
      <PopoverContent align="center" side="bottom" className="w-80">
        <LoginBox />
      </PopoverContent>
    </Popover>
  );
}
