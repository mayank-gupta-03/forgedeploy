import { useEffect, useState } from 'react';
import { Loader2, AlertCircle } from 'lucide-react';
import { setToken } from '@/services/api';

interface RedirectHandlerProps {
  onLoginSuccess: (token: string) => void;
  onNavigateToLogin: () => void;
}

export default function RedirectHandler({ onLoginSuccess, onNavigateToLogin }: RedirectHandlerProps) {
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');

    if (token) {
      setToken(token);
      onLoginSuccess(token);
    } else {
      setError('OAuth authentication failed. No token found in the redirect URL.');
      setTimeout(() => {
        onNavigateToLogin();
      }, 3000);
    }
  }, [onLoginSuccess, onNavigateToLogin]);

  return (
    <div className="relative min-h-screen w-full flex items-center justify-center bg-[#0a0a0f] text-slate-100 overflow-hidden px-4">
      {/* Background decorations */}
      <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] rounded-full bg-violet-950/20 blur-[120px]" />
      
      <div className="text-center z-10 space-y-4">
        {error ? (
          <div className="flex flex-col items-center space-y-2">
            <AlertCircle className="h-10 w-10 text-red-500 animate-bounce" />
            <h2 className="text-xl font-semibold text-white">Authentication Failed</h2>
            <p className="text-sm text-slate-400 max-w-xs">{error}</p>
            <p className="text-xs text-violet-400">Redirecting to login page...</p>
          </div>
        ) : (
          <div className="flex flex-col items-center space-y-3">
            <Loader2 className="h-12 w-12 text-violet-500 animate-spin" />
            <h2 className="text-xl font-semibold text-white">Completing GitHub authentication</h2>
            <p className="text-sm text-slate-400">Please wait while we secure your session...</p>
          </div>
        )}
      </div>
    </div>
  );
}
