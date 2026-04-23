'use client';

import { useEffect } from 'react';

declare global {
  interface Window {
    fbq?: (...args: unknown[]) => void;
  }
}

function track(event: string, params?: Record<string, unknown>) {
  if (typeof window !== 'undefined' && window.fbq) {
    window.fbq('track', event, params ?? {});
  }
}

/** Fire ViewContent when a blog post mounts */
export function PixelBlogView({ title, slug }: { title: string; slug: string }) {
  useEffect(() => {
    track('ViewContent', { content_name: title, content_ids: [slug], content_type: 'article' });
  }, [title, slug]);
  return null;
}

/** Download button that fires Lead on click */
export function PixelDownloadButton({
  href,
  platform,
  className,
  children,
}: {
  href: string;
  platform: 'ios' | 'android';
  className?: string;
  children: React.ReactNode;
}) {
  return (
    <a
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className={className}
      onClick={() => track('Lead', { content_name: 'app_download', platform })}
    >
      {children}
    </a>
  );
}
