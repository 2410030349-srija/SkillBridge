import Card from '../components/Card'

function DashboardLayout({ title, subtitle, rightContent, children }) {
  return (
    <main className="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6">
      <Card className="mb-6 bg-gradient-to-r from-amber-50 via-white to-orange-50">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="font-heading text-2xl font-bold text-slate-900 sm:text-3xl">{title}</h1>
            <p className="mt-2 text-sm text-slate-600 sm:text-base">{subtitle}</p>
          </div>
          {rightContent ? <div>{rightContent}</div> : null}
        </div>
      </Card>
      {children}
    </main>
  )
}

export default DashboardLayout
