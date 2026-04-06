function FormInput({ id, label, type = 'text', value, onChange, placeholder, options, multiline = false, min, max }) {
  const baseClass = 'w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm text-slate-900 shadow-sm outline-none transition focus:border-amber-400 focus:ring-2 focus:ring-amber-100'

  return (
    <label htmlFor={id} className="block">
      <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</span>
      {options ? (
        <select id={id} className={baseClass} value={value} onChange={onChange}>
          {options.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      ) : multiline ? (
        <textarea id={id} className={`${baseClass} min-h-24`} value={value} onChange={onChange} placeholder={placeholder} />
      ) : (
        <input
          id={id}
          className={baseClass}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          min={min}
          max={max}
        />
      )}
    </label>
  )
}

export default FormInput
