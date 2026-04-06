import { useEffect, useMemo, useState } from 'react'
import Card from '../components/Card'
import FormInput from '../components/FormInput'
import DashboardLayout from '../layouts/DashboardLayout'
import { api } from '../services/api'
import { getProfile } from '../services/auth'

function ContentPreview({ item }) {
  return (
    <article className="rounded-xl border border-slate-200 p-4">
      <h3 className="font-heading text-base font-bold text-slate-900">{item.title}</h3>
      <p className="mt-1 text-sm text-slate-600">{item.description}</p>
      <div className="mt-2 flex flex-wrap gap-2">
        {(item.tags || []).map((tag) => (
          <span key={tag} className="rounded-full bg-amber-50 px-2 py-1 text-xs font-semibold text-amber-700">
            {tag}
          </span>
        ))}
      </div>
      {item.resourceLink ? (
        <a
          href={item.resourceLink}
          target="_blank"
          rel="noreferrer"
          className="mt-3 inline-flex items-center rounded-lg border border-amber-300 bg-amber-50 px-3 py-1.5 text-xs font-bold text-amber-700"
        >
          Open Resource
        </a>
      ) : null}
      {!item.resourceLink && item.resourceFile ? (
        <p className="mt-3 text-xs text-slate-500">File: {item.resourceFile}</p>
      ) : null}
      <p className="mt-3 text-xs text-slate-500">#{item.id} • {item.domain}</p>
    </article>
  )
}

function LearnerDashboard() {
  const [items, setItems] = useState([])
  const [bookmarks, setBookmarks] = useState([])
  const [loadedDetail, setLoadedDetail] = useState(null)
  const [search, setSearch] = useState('')
  const [feedback, setFeedback] = useState({ contentId: '', rating: 5, comment: '' })
  const [myFeedback, setMyFeedback] = useState([])
  const [actionMsg, setActionMsg] = useState('')
  const [detailsId, setDetailsId] = useState('')
  const runSearch = async () => {
    try {
      const keyword = search.trim()
      if (!keyword) {
        await loadTrending()
        setActionMsg('Loaded trending content. Enter a keyword to narrow results.')
        return
      }
      const data = await api.searchContent(keyword)
      setItems(data)
      setActionMsg(data.length ? `Found ${data.length} result(s).` : 'No results found for your keyword.')
    } catch (error) {
      setActionMsg(error.message)
    }
  }

  const loadTrending = async () => {
    try {
      const data = await api.feed()
      setItems(data)
      setActionMsg(data.length ? `Loaded ${data.length} item(s) from your feed.` : 'No approved content available in your feed yet.')
    } catch (error) {
      setActionMsg(error.message)
    }
  }

  const react = async (contentId, type) => {
    try {
      await api.reactContent(contentId, type)
      setActionMsg(`Saved ${type.toLowerCase()} on #${contentId}.`)
    } catch (error) {
      setActionMsg(error.message)
    }
  }

  const bookmark = async (contentId) => {
    try {
      await api.bookmarkContent(contentId)
      setActionMsg(`Bookmarked content #${contentId}.`)
    } catch (error) {
      setActionMsg(error.message)
    }
  }

  const removeBookmark = async (contentId) => {
    try {
      await api.removeBookmark(contentId)
      setActionMsg(`Removed bookmark for #${contentId}.`)
      const latest = await api.bookmarks()
      setBookmarks(latest)
    } catch (error) {
      setActionMsg(error.message)
    }
  }

  const loadBookmarks = async () => {
    try {
      const latest = await api.bookmarks()
      setBookmarks(latest)
      setActionMsg(`Loaded ${latest.length} bookmark(s).`)
    } catch (error) {
      setActionMsg(error.message)
    }
  }

  const loadDetails = async () => {
    const id = Number(detailsId)
    if (!id) {
      setActionMsg('Enter a valid content ID to load details.')
      return
    }
    try {
      const detail = await api.contentDetails(id)
      setLoadedDetail(detail)
      setActionMsg(`Loaded #${detail.id} views:${detail.views} likes:${detail.likes} rating:${Number(detail.averageRating).toFixed(1)}`)
    } catch (error) {
      setActionMsg(error.message)
    }
  }

  const submitFeedback = async () => {
    try {
      await api.rateContent(Number(feedback.contentId), {
        rating: Number(feedback.rating),
        comment: feedback.comment,
      })
      setActionMsg(`Feedback submitted for #${feedback.contentId}.`)
      setFeedback((prev) => ({ ...prev, comment: '' }))
    } catch (error) {
      setActionMsg(error.message)
    }
  }

  const loadMyFeedback = async () => {
    try {
      const data = await api.myFeedback()
      setMyFeedback(data)
      setActionMsg(data.length ? `Loaded ${data.length} submitted feedback.` : 'No feedback submitted yet.')
    } catch (error) {
      setActionMsg(error.message)
    }
  }

  return (
    <div className="grid gap-4 lg:grid-cols-3">
      <Card className="lg:col-span-2" title="Trending in Your Domain" subtitle="Feed updates with your searches and interactions.">
        <div className="mb-4 flex gap-2">
          <button type="button" onClick={loadTrending} className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-bold text-slate-700">
            Load Trending
          </button>
          <input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Search topics"
            className="w-full rounded-xl border border-slate-200 px-3 py-2"
          />
          <button type="button" onClick={runSearch} className="rounded-xl bg-amber-500 px-4 py-2 text-sm font-bold text-white">
            Search
          </button>
        </div>

        <div className="space-y-3">
          <div className="flex flex-wrap items-end gap-2">
            <div className="w-full sm:w-48">
              <FormInput id="details-content-id" label="Load Details By ID" value={detailsId} onChange={(event) => setDetailsId(event.target.value)} />
            </div>
            <button type="button" onClick={loadDetails} className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-bold text-slate-700">
              Load Details
            </button>
            <button type="button" onClick={loadBookmarks} className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-bold text-slate-700">
              Load Bookmarks
            </button>
          </div>
          {items.map((item) => (
            <div key={item.id} className="rounded-xl border border-slate-200 p-4">
              <ContentPreview item={item} />
              <div className="mt-3 flex flex-wrap gap-2">
                <button type="button" onClick={() => react(item.id, 'LIKE')} className="rounded-lg bg-slate-900 px-3 py-1.5 text-xs font-bold text-white">
                  Like
                </button>
                <button type="button" onClick={() => bookmark(item.id)} className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-bold text-slate-700">
                  Bookmark
                </button>
                <button type="button" onClick={() => removeBookmark(item.id)} className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-bold text-slate-700">
                  Remove Bookmark
                </button>
              </div>
            </div>
          ))}
          {!items.length ? <p className="text-sm text-slate-500">No content yet.</p> : null}
          {bookmarks.length ? (
            <div className="rounded-xl border border-slate-200 p-3">
              <p className="mb-2 text-xs font-bold uppercase tracking-wide text-slate-400">My Bookmarks</p>
              <ul className="space-y-1 text-sm text-slate-600">
                {bookmarks.map((item) => (
                  <li key={item.id}>#{item.id} {item.title}</li>
                ))}
              </ul>
            </div>
          ) : null}
          {loadedDetail ? (
            <div className="rounded-xl border border-slate-200 p-3">
              <p className="mb-2 text-xs font-bold uppercase tracking-wide text-slate-400">Loaded Details</p>
              <p className="text-sm font-semibold text-slate-800">#{loadedDetail.id} {loadedDetail.title}</p>
              <p className="text-sm text-slate-600">Views: {loadedDetail.views} | Likes: {loadedDetail.likes} | Rating: {Number(loadedDetail.averageRating).toFixed(1)}</p>
            </div>
          ) : null}
        </div>
      </Card>

      <Card title="Rate & Feedback" subtitle="Give rating (1-5) and text feedback.">
        <div className="space-y-3">
          <FormInput id="feedback-content-id" label="Content ID" value={feedback.contentId} onChange={(event) => setFeedback((prev) => ({ ...prev, contentId: event.target.value }))} />
          <FormInput id="feedback-rating" label="Rating" type="number" min={1} max={5} value={feedback.rating} onChange={(event) => setFeedback((prev) => ({ ...prev, rating: event.target.value }))} />
          <FormInput id="feedback-comment" label="Feedback" multiline value={feedback.comment} onChange={(event) => setFeedback((prev) => ({ ...prev, comment: event.target.value }))} />
          <div className="flex gap-2">
            <button type="button" onClick={submitFeedback} className="flex-1 rounded-xl bg-amber-500 px-4 py-2.5 text-sm font-bold text-white">
              Submit Feedback
            </button>
            <button type="button" onClick={loadMyFeedback} className="flex-1 rounded-xl border border-slate-200 px-4 py-2 text-sm font-bold text-slate-700">
              My Feedback
            </button>
          </div>
          {actionMsg ? <p className="text-sm font-semibold text-emerald-600">{actionMsg}</p> : null}
          {myFeedback.length ? (
            <div className="rounded-xl border border-slate-200 p-3">
              <p className="mb-2 text-xs font-bold uppercase tracking-wide text-slate-400">My Submitted Feedback</p>
              <div className="space-y-2">
                {myFeedback.map((item) => (
                  <div key={item.id} className="rounded-lg border border-slate-100 p-2 text-sm">
                    <p className="font-semibold text-slate-800">#{item.contentId} {item.contentTitle}</p>
                    <p className="text-xs text-amber-600">★ {item.rating}/5</p>
                    <p className="mt-1 text-xs text-slate-600">{item.comment}</p>
                  </div>
                ))}
              </div>
            </div>
          ) : null}
        </div>
      </Card>
    </div>
  )
}

function CreatorDashboard({ isVerified }) {
  const [content, setContent] = useState({
    title: '',
    description: '',
    domain: 'SOFTWARE DEVELOPMENT',
    tags: '',
    resourceLink: '',
    resourceFile: '',
  })
  const [myContent, setMyContent] = useState([])
  const [myFeedback, setMyFeedback] = useState([])
  const [msg, setMsg] = useState('')
  const [editId, setEditId] = useState('')

  const upload = async () => {
    try {
      await api.createContent({
        ...content,
        tags: content.tags.split(',').map((tag) => tag.trim()).filter(Boolean),
      })
      setMsg('Content uploaded successfully.')
      const fresh = await api.myContent()
      setMyContent(fresh)
    } catch (error) {
      setMsg(error.message)
    }
  }

  const loadMine = async () => {
    try {
      const fresh = await api.myContent()
      setMyContent(fresh)
      setMsg(fresh.length ? `Loaded ${fresh.length} uploaded item(s).` : 'No uploaded content yet.')
    } catch (error) {
      setMsg(error.message)
    }
  }

  const update = async () => {
    const id = Number(editId)
    if (!id) {
      setMsg('Enter a valid Content ID to update.')
      return
    }
    try {
      await api.updateContent(id, {
        ...content,
        tags: content.tags.split(',').map((tag) => tag.trim()).filter(Boolean),
      })
      setMsg(`Content #${id} updated.`)
      await loadMine()
    } catch (error) {
      setMsg(error.message)
    }
  }

  const remove = async (contentId) => {
    const id = Number(contentId ?? editId)
    if (!id) {
      setMsg('Enter a valid Content ID to delete.')
      return
    }
    try {
      await api.deleteContent(id)
      setMsg(`Content #${id} deleted.`)
      await loadMine()
    } catch (error) {
      setMsg(error.message)
    }
  }

  const loadMyFeedback = async () => {
    try {
      const data = await api.myFeedback()
      setMyFeedback(data)
      setMsg(data.length ? `Loaded ${data.length} feedback item(s).` : 'No feedback submitted yet.')
    } catch (error) {
      setMsg(error.message)
    }
  }

  const pending = useMemo(() => myContent.filter((item) => !item.verified), [myContent])
  const approved = useMemo(() => myContent.filter((item) => item.verified), [myContent])

  return (
    <div className="grid gap-4 lg:grid-cols-3">
      <Card className="lg:col-span-2" title="Upload Content" subtitle="Title, description, domain, tags, and link/file.">
        {!isVerified ? (
          <p className="mb-3 rounded-lg bg-amber-50 px-3 py-2 text-sm font-semibold text-amber-700">
            You must verify your email before publishing
          </p>
        ) : (
          <p className="mb-3 rounded-lg bg-emerald-50 px-3 py-2 text-sm font-semibold text-emerald-700">
            Verified account. You can publish now.
          </p>
        )}
        <div className="grid gap-3 sm:grid-cols-2">
          <FormInput id="creator-title" label="Title" value={content.title} onChange={(event) => setContent((prev) => ({ ...prev, title: event.target.value }))} />
          <FormInput
            id="creator-domain"
            label="Domain"
            options={[
              { value: 'SOFTWARE DEVELOPMENT', label: 'Web Development' },
              { value: 'DATA SCIENCE', label: 'AI / ML' },
              { value: 'DEVOPS', label: 'Cloud Computing' },
              { value: 'BLOCKCHAIN', label: 'Cybersecurity' },
            ]}
            value={content.domain}
            onChange={(event) => setContent((prev) => ({ ...prev, domain: event.target.value }))}
          />
          <div className="sm:col-span-2">
            <FormInput id="creator-description" label="Description" multiline value={content.description} onChange={(event) => setContent((prev) => ({ ...prev, description: event.target.value }))} />
          </div>
          <FormInput id="creator-tags" label="Tags" value={content.tags} onChange={(event) => setContent((prev) => ({ ...prev, tags: event.target.value }))} placeholder="#AI,#Web" />
          <FormInput id="creator-link" label="Link / File" value={content.resourceLink} onChange={(event) => setContent((prev) => ({ ...prev, resourceLink: event.target.value }))} placeholder="https://..." />
          <FormInput id="creator-edit-id" label="Content ID (for update/delete)" value={editId} onChange={(event) => setEditId(event.target.value)} placeholder="12" />
        </div>
        <div className="mt-3 flex flex-wrap gap-2">
          <button type="button" onClick={upload} className="rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-bold text-white">
            Upload Content
          </button>
          <button type="button" onClick={update} className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-bold text-slate-700">
            Update Content
          </button>
          <button type="button" onClick={() => remove()} className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-bold text-slate-700">
            Delete Content
          </button>
        </div>
        {msg ? <p className="mt-2 text-sm font-semibold text-slate-600">{msg}</p> : null}
      </Card>

      <Card title="Approval Status">
        <button type="button" onClick={loadMine} className="mb-3 rounded-xl border border-slate-200 px-3 py-2 text-sm font-bold text-slate-700">Load / Refresh</button>
        <p className="mb-1 text-xs font-bold uppercase tracking-wide text-slate-400">Pending Approval</p>
        <ul className="mb-4 space-y-2 text-sm text-slate-600">
          {pending.map((item) => (
            <li key={item.id} className="flex items-center justify-between gap-3">
              <span>#{item.id} {item.title}</span>
              <button type="button" onClick={() => remove(item.id)} className="rounded-lg border border-slate-200 px-2 py-1 text-xs font-bold text-slate-700">
                Delete
              </button>
            </li>
          ))}
          {!pending.length ? <li>No pending items</li> : null}
        </ul>

        <p className="mb-1 text-xs font-bold uppercase tracking-wide text-slate-400">Approved Content</p>
        <ul className="space-y-2 text-sm text-slate-600">
          {approved.map((item) => (
            <li key={item.id} className="flex items-center justify-between gap-3">
              <span>#{item.id} {item.title}</span>
              <button type="button" onClick={() => remove(item.id)} className="rounded-lg border border-slate-200 px-2 py-1 text-xs font-bold text-slate-700">
                Delete
              </button>
            </li>
          ))}
          {!approved.length ? <li>No approved items</li> : null}
        </ul>
      </Card>

      <Card title="My Feedback">
        <button type="button" onClick={loadMyFeedback} className="mb-3 rounded-xl border border-slate-200 px-3 py-2 text-sm font-bold text-slate-700">Load My Feedback</button>
        {myFeedback.length ? (
          <div className="space-y-2">
            {myFeedback.map((item) => (
              <div key={item.id} className="rounded-lg border border-slate-100 p-2 text-sm">
                <p className="font-semibold text-slate-800">#{item.contentId} {item.contentTitle}</p>
                <p className="text-xs text-amber-600">★ {item.rating}/5</p>
                <p className="mt-1 text-xs text-slate-600">{item.comment}</p>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-slate-500">No feedback submitted yet.</p>
        )}
      </Card>
    </div>
  )
}

function AdminDashboard() {
  const [pending, setPending] = useState([])
  const [users, setUsers] = useState([])

  const loadAll = async () => {
    const [pendingData, userData] = await Promise.all([api.pendingContent(), api.users()])
    setPending(pendingData)
    setUsers(userData)
  }

  const approve = async (id) => {
    await api.approveContent(id)
    await loadAll()
  }

  const reject = async (id) => {
    await api.rejectContent(id)
    await loadAll()
  }

  return (
    <div className="grid gap-4 lg:grid-cols-2">
      <Card title="Uploaded Content Moderation" subtitle="Approve or reject submitted content.">
        <button type="button" onClick={loadAll} className="mb-3 rounded-xl border border-slate-200 px-3 py-2 text-sm font-bold text-slate-700">Load Moderation Data</button>
        <div className="space-y-3">
          {pending.map((item) => (
            <div key={item.id} className="rounded-xl border border-slate-200 p-3">
              <p className="font-semibold text-slate-900">#{item.id} {item.title}</p>
              <p className="text-xs text-slate-500">{item.domain}</p>
              <div className="mt-2 flex gap-2">
                <button type="button" onClick={() => approve(item.id)} className="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-bold text-white">
                  Approve
                </button>
                <button type="button" onClick={() => reject(item.id)} className="rounded-lg bg-rose-600 px-3 py-1.5 text-xs font-bold text-white">
                  Reject
                </button>
              </div>
            </div>
          ))}
          {!pending.length ? <p className="text-sm text-slate-500">No pending content.</p> : null}
        </div>
      </Card>

      <Card title="Users List" subtitle="Basic moderation panel with user visibility.">
        <ul className="space-y-3">
          {users.map((user) => (
            <li key={user.id} className="rounded-xl border border-slate-200 p-3 text-sm">
              <p className="font-semibold text-slate-900">{user.username}</p>
              <p className="text-slate-500">{user.email}</p>
              <p className="text-xs text-slate-500">Role: {user.role}</p>
            </li>
          ))}
          {!users.length ? <li className="text-sm text-slate-500">No users loaded.</li> : null}
        </ul>
      </Card>
    </div>
  )
}

function DashboardPage() {
  const [profile, setProfile] = useState(getProfile())
  const [roleDraft, setRoleDraft] = useState((getProfile()?.role || 'LEARNER').toUpperCase())
  const [domainDraft, setDomainDraft] = useState(getProfile()?.domain || 'DATA SCIENCE')
  const [domains, setDomains] = useState([])
  const [roleMessage, setRoleMessage] = useState('')
  const [domainMessage, setDomainMessage] = useState('')
  const [verifyMessage, setVerifyMessage] = useState('')
  const [health, setHealth] = useState({ status: 'checking', text: 'Click refresh to check backend' })

  useEffect(() => {
    api.domains().then(setDomains).catch(() => {})
  }, [])

  const switchRole = async () => {
    try {
      const updated = await api.updateRole(roleDraft)
      localStorage.setItem('skillbridge.profile', JSON.stringify(updated))
      setProfile(updated)
      setRoleMessage(`Role updated to ${updated.role}`)
    } catch (error) {
      setRoleMessage(error.message)
    }
  }

  const verifyEmail = async () => {
    try {
      const updated = await api.verifyEmail()
      localStorage.setItem('skillbridge.profile', JSON.stringify(updated))
      setProfile(updated)
      setVerifyMessage('Email verified successfully. You can now publish as Creator.')
    } catch (error) {
      setVerifyMessage(error.message)
    }
  }

  const updateDomain = async () => {
    try {
      const updated = await api.updateDomain(domainDraft)
      localStorage.setItem('skillbridge.profile', JSON.stringify(updated))
      setProfile(updated)
      setDomainDraft(updated.domain || domainDraft)
      setDomainMessage(`Domain updated to ${updated.domain}`)
    } catch (error) {
      setDomainMessage(error.message)
    }
  }

  const checkHealth = async () => {
    try {
      await api.health()
      setHealth({ status: 'up', text: 'Backend Connected' })
    } catch {
      setHealth({ status: 'down', text: 'Backend Offline' })
    }
  }

  if (!profile) {
    return (
      <DashboardLayout title="Dashboard" subtitle="Please login first.">
        <Card><p className="text-sm text-slate-600">No active session.</p></Card>
      </DashboardLayout>
    )
  }

  const role = (profile.role || 'LEARNER').toUpperCase()
  const subtitle = `Role: ${role} | Domain: ${profile.domain || 'Not set yet'}`

  const healthBadgeClass =
    health.status === 'up'
      ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
      : health.status === 'down'
        ? 'border-rose-200 bg-rose-50 text-rose-700'
        : 'border-amber-200 bg-amber-50 text-amber-700'

  return (
    <DashboardLayout
      title="Dashboard"
      subtitle={subtitle}
      rightContent={(
        <div className="flex items-center gap-2">
          <span className={`inline-flex items-center rounded-full border px-3 py-1 text-xs font-bold ${healthBadgeClass}`}>
            {health.text}
          </span>
          <button type="button" onClick={checkHealth} className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-bold text-slate-700">
            Refresh
          </button>
        </div>
      )}
    >
      <Card title="Account Role" subtitle="Already logged in users can switch role any time.">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
          <FormInput
            id="switch-role"
            label="Select Role"
            options={[
              { value: 'LEARNER', label: 'Learner' },
              { value: 'CREATOR', label: 'Creator' },
            ]}
            value={roleDraft}
            onChange={(event) => setRoleDraft(event.target.value)}
          />
          <button type="button" onClick={switchRole} className="rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-bold text-white">
            Update Role
          </button>
        </div>
        <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-end">
          <FormInput
            id="switch-domain"
            label="Select Domain"
            options={domains.length ? domains.map((domain) => ({ value: domain, label: domain })) : [
              { value: 'DATA SCIENCE', label: 'Data Science' },
              { value: 'SOFTWARE DEVELOPMENT', label: 'Software Development' },
              { value: 'DEVOPS', label: 'DevOps' },
              { value: 'BLOCKCHAIN', label: 'Blockchain' },
            ]}
            value={domainDraft}
            onChange={(event) => setDomainDraft(event.target.value)}
          />
          <button type="button" onClick={updateDomain} className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-bold text-slate-700">
            Update Domain
          </button>
        </div>
        <div className="mt-3 flex flex-col gap-2 sm:flex-row sm:items-center">
          <button type="button" onClick={verifyEmail} className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-bold text-slate-700">
            Verify Email
          </button>
          <span className={`text-sm font-semibold ${profile.verified ? 'text-emerald-600' : 'text-amber-700'}`}>
            {profile.verified ? 'Verified' : 'Not verified'}
          </span>
        </div>
        {roleMessage ? <p className="mt-2 text-sm font-semibold text-emerald-600">{roleMessage}</p> : null}
        {domainMessage ? <p className="mt-1 text-sm font-semibold text-emerald-600">{domainMessage}</p> : null}
        {verifyMessage ? <p className="mt-1 text-sm font-semibold text-emerald-600">{verifyMessage}</p> : null}
      </Card>

      {role === 'CREATOR' ? <CreatorDashboard isVerified={Boolean(profile.verified)} /> : null}
      {role === 'ADMIN' ? <AdminDashboard /> : null}
      {role === 'LEARNER' ? <LearnerDashboard /> : null}
    </DashboardLayout>
  )
}

export default DashboardPage
