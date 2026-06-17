type VerifiedBadgeSize = 'sm' | 'md' | 'lg'

interface VerifiedBadgeProps {
  title?: string
  size?: VerifiedBadgeSize
}

export function VerifiedBadge({ title = 'Cuenta verificada', size = 'md' }: VerifiedBadgeProps) {
  return (
    <span
      className={`verified-badge verified-badge--${size}`}
      title={title}
      aria-label={title}
      role="img"
    >
      <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path
          className="verified-badge-shield"
          d="M12 2.25 4.5 5.75v5.5c0 4.65 3.15 7.55 7.5 8.75 4.35-1.2 7.5-4.1 7.5-8.75v-5.5L12 2.25Z"
        />
        <path
          className="verified-badge-shine"
          d="M12 3.5 6.25 6.35v4.9c0 3.55 2.35 5.85 5.75 6.85.4.12.8.2 1.2.25V3.5H12Z"
          opacity="0.22"
        />
        <path
          className="verified-badge-check"
          d="m10.35 13.65-1.85-1.85-.95.95 2.8 2.8 5.35-5.35-.95-.95-4.4 4.4Z"
        />
      </svg>
    </span>
  )
}
