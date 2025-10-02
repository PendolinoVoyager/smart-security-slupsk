"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Camera, Eye, Shield, Cpu } from "lucide-react";
import { motion } from "framer-motion";
import Link from "next/link";
import { ENDPOINTS } from "@/api/config";

export default function HomePagePromo() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-gray-50 to-white text-gray-900">
      {/* Hero Section */}
      <section className="flex flex-col items-center justify-center text-center py-24 px-6">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          <div className="flex items-center justify-center gap-2 mb-4">
            <Cpu className="w-8 h-8 text-blue-600" />
            <h1 className="text-4xl font-bold sm:text-5xl">Smart Security</h1>
          </div>
          <p className="max-w-2xl text-lg text-gray-600 mb-8">
            The next generation of AI-powered security cameras — see everything,
            recognize everyone, and stay secure.
          </p>
          <Button
            size="lg"
            className="bg-blue-600 hover:bg-blue-700 text-white rounded-full px-6"
          >
            <Link href="/auth/register">Get Started</Link>
          </Button>
        </motion.div>
      </section>

      {/* Features Section */}
      <section className="py-16 px-6 max-w-5xl mx-auto grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
        {[
          {
            title: "Live Preview",
            icon: Eye,
            description:
              "Monitor your space in real-time from any device, anywhere in the world.",
          },
          {
            title: "Object & Face Recognition",
            icon: Camera,
            description:
              "AI detects and recognizes people, vehicles, and packages automatically.",
          },
          {
            title: "Advanced Security",
            icon: Shield,
            description:
              "Encrypted cloud storage and instant alerts to keep you informed 24/7.",
          },
        ].map((feature, i) => (
          <motion.div
            key={i}
            initial={{ opacity: 0, y: 15 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.1, duration: 0.4 }}
          >
            <Card className="hover:shadow-lg transition-shadow rounded-2xl">
              <CardHeader>
                <feature.icon className="w-10 h-10 text-blue-600 mb-2" />
                <CardTitle>{feature.title}</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-gray-600">{feature.description}</p>
              </CardContent>
            </Card>
          </motion.div>
        ))}
      </section>

      {/* CTA Section */}
      <section className="bg-blue-600 text-white py-16 text-center">
        <h2 className="text-3xl font-semibold mb-4">
          Ready to make your home smarter?
        </h2>
        <p className="mb-6 text-blue-100">
          Join thousands of users protecting their spaces with AI-driven
          surveillance.
        </p>
        <Button
          size="lg"
          variant="secondary"
          className="text-blue-700 font-semibold rounded-full"
        >
          <Link href="/auth/register">Try It Now</Link>
        </Button>
      </section>

      {/* Footer */}
      <footer className="py-8 text-center text-gray-500 text-sm">
        © {new Date().getFullYear()} SmartVision AI — All rights reserved.
      </footer>
    </div>
  );
}
