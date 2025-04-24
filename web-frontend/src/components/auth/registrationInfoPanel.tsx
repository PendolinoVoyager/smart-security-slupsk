"use client";
import { motion } from "motion/react";

export default function RegistrationInfoPanel() {
  return (
    <div
      className="h-full flex flex-1 flex-col items-center pt-24 justify-center"
      style={{
        background:
          "linear-gradient(135deg, hsl(var(--primary-dark)), hsl(var(--primary)))",
      }}
    >
      <motion.h1
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, ease: "easeOut" }}
        className="text-4xl font-bold text-center"
      >
        Join Our <span className="text-primary font-bold">AI</span>
        -Powered Community! 🎉
      </motion.h1>

      <motion.p
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3, duration: 0.8, ease: "easeOut" }}
        className="text-lg mt-8 text-center max-w-sm"
      >
        Register today to experience seamless communication with the power of{" "}
        <span className="text-primary font-bold">AI</span> at your fingertips.
      </motion.p>
    </div>
  );
}
