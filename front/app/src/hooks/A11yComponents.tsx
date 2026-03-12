import type React from 'react';

interface SkipLinkProps {
  targetId: string;
  children: React.ReactNode;
}

export function SkipLink({ targetId, children }: SkipLinkProps) {
  return (
    <a
      href={`#${targetId}`}
      className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 focus:px-4 focus:py-2 focus:bg-orange-500 focus:text-white focus:rounded-lg"
    >
      {children}
    </a>
  );
}

interface VisuallyHiddenProps {
  children: React.ReactNode;
}

export function VisuallyHidden({ children }: VisuallyHiddenProps) {
  return <span className="sr-only">{children}</span>;
}
