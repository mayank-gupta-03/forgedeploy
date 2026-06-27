import React, { useState } from 'react';
import { Mail, Lock, ArrowRight, Loader2, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { api } from '@/services/api';

interface RegisterProps {
  onNavigateToLogin: (registeredEmail?: string) => void;
}

export default function Register({ onNavigateToLogin }: RegisterProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password || !confirmPassword) return;

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setIsLoading(true);
    setError(null);
    setSuccess(null);

    try {
      await api.register(email, password);
      setSuccess('Account created successfully! Redirecting to login...');
      setTimeout(() => {
        onNavigateToLogin(email);
      }, 1500);
    } catch (err: any) {
      setError(err.message || 'Registration failed. Email may already be in use.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen w-full flex items-center justify-center bg-[#0a0a0f] text-slate-100 overflow-hidden px-4">
      {/* Decorative background */}
      <div className="absolute top-[-10%] right-[-10%] w-[50%] h-[50%] rounded-full bg-violet-950/20 blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-10%] left-[-10%] w-[50%] h-[50%] rounded-full bg-indigo-950/20 blur-[120px] pointer-events-none" />

      {/* Grid background effect */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#1f29370a_1px,transparent_1px),linear-gradient(to_bottom,#1f29370a_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)] pointer-events-none opacity-40" />

      <Card className="w-full max-w-md border-slate-800 bg-slate-900/60 backdrop-blur-xl shadow-2xl relative z-10">
        <CardHeader className="space-y-1 text-center">
          <button
            onClick={() => onNavigateToLogin()}
            className="flex items-center text-xs text-slate-400 hover:text-slate-200 transition-colors mb-4 group"
            type="button"
          >
            <ArrowLeft className="mr-1 h-3.5 w-3.5 group-hover:-translate-x-0.5 transition-transform" />
            Back to login
          </button>
          <CardTitle className="text-2xl font-bold tracking-tight text-white">Create an account</CardTitle>
          <CardDescription className="text-slate-400">
            Enter your details to register a new account on ForgeDeploy
          </CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4">
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="p-3 text-sm text-red-400 bg-red-950/20 border border-red-900/50 rounded-lg">
                {error}
              </div>
            )}
            {success && (
              <div className="p-3 text-sm text-emerald-400 bg-emerald-950/20 border border-emerald-900/50 rounded-lg">
                {success}
              </div>
            )}
            <div className="space-y-2">
              <Label htmlFor="email" className="text-slate-300">Email Address</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-3 h-4 w-4 text-slate-500" />
                <Input
                  id="email"
                  type="email"
                  placeholder="name@example.com"
                  className="pl-10 bg-slate-950/40 border-slate-800 text-slate-100 placeholder-slate-500 focus-visible:ring-violet-500 focus-visible:border-violet-500"
                  value={email}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEmail(e.target.value)}
                  disabled={isLoading}
                  required
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="password" className="text-slate-300">Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-3 h-4 w-4 text-slate-500" />
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  className="pl-10 bg-slate-950/40 border-slate-800 text-slate-100 placeholder-slate-500 focus-visible:ring-violet-500 focus-visible:border-violet-500"
                  value={password}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setPassword(e.target.value)}
                  disabled={isLoading}
                  required
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword" className="text-slate-300">Confirm Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-3 h-4 w-4 text-slate-500" />
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  className="pl-10 bg-slate-950/40 border-slate-800 text-slate-100 placeholder-slate-500 focus-visible:ring-violet-500 focus-visible:border-violet-500"
                  value={confirmPassword}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setConfirmPassword(e.target.value)}
                  disabled={isLoading}
                  required
                />
              </div>
            </div>
            <Button
              type="submit"
              className="w-full bg-violet-600 hover:bg-violet-500 text-white transition-all duration-200 h-11 relative overflow-hidden group"
              disabled={isLoading}
            >
              {isLoading ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <span className="flex items-center justify-center">
                  Register <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </span>
              )}
            </Button>
          </form>
        </CardContent>
        <CardFooter className="flex flex-wrap items-center justify-center gap-1 border-t border-slate-800/60 mt-2 pt-4 text-sm text-slate-400">
          Already have an account?{' '}
          <button
            onClick={() => onNavigateToLogin()}
            className="text-violet-400 hover:text-violet-300 font-medium underline underline-offset-4 transition-colors"
          >
            Sign in
          </button>
        </CardFooter>
      </Card>
    </div>
  );
}
