'use client';

import { useEffect } from 'react';

declare global {
  interface Window { fbq?: (...args: unknown[]) => void; }
}

const APP_STORE = 'https://apps.apple.com/app/life-planner-ai-coach/id6745726864';
const PLAY_STORE = 'https://play.google.com/store/apps/details?id=az.tribe.lifeplanner';
const FALLBACK = '/';

export default function DownloadRedirect() {
  useEffect(() => {
    const ua = navigator.userAgent || '';
    const platform = /iPad|iPhone|iPod/.test(ua) ? 'ios' : /Android/i.test(ua) ? 'android' : null;

    if (window.fbq) {
      window.fbq('track', 'Lead', { content_name: 'app_download', platform: platform ?? 'unknown' });
    }

    if (platform === 'ios') {
      window.location.href = APP_STORE;
    } else if (platform === 'android') {
      window.location.href = PLAY_STORE;
    } else {
      window.location.href = FALLBACK;
    }
  }, []);

  return (
    <div className="min-h-screen flex items-center justify-center dark:bg-[#0a0a0f] bg-gray-50">
      <p className="text-lg dark:text-white/60 text-gray-500">Redirecting to store...</p>
    </div>
  );
}
