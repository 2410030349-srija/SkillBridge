function Card({ title, subtitle, children, actions, className = '' }) {
  return (
    <section className={`rounded-2xl border border-amber-100 bg-white p-5 shadow-sm ${className}`}>
      {(title || subtitle || actions) && (
        <header className="mb-4 flex items-start justify-between gap-3">
          <div>
            {title ? <h2 className="font-heading text-lg font-bold text-slate-900">{title}</h2> : null}
            {subtitle ? <p className="mt-1 text-sm text-slate-500">{subtitle}</p> : null}
          </div>
          {actions ? <div>{actions}</div> : null}
        </header>
      )}
      {children}
    </section>
  )
}

export default Card
