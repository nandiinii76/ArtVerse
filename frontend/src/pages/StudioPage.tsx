import { FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createArtwork } from '@/lib/artworks'

export function StudioPage() {
  const navigate = useNavigate()
  const [message, setMessage] = useState('')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [imageUrl, setImageUrl] = useState('')
  const [category, setCategory] = useState('Painting')
  const [medium, setMedium] = useState('')
  const [style, setStyle] = useState('')
  const [year, setYear] = useState('')
  const [price, setPrice] = useState('')

  async function submit(e: FormEvent) {
    e.preventDefault(); setMessage('Saving work...')
    try {
      const a = await createArtwork({ title, description, imageUrl, category, medium, style,
        yearCreated: year ? Number(year) : undefined, price: price ? Number(price) : undefined,
        currency: 'INR', status: 'PUBLISHED' })
      navigate(`/artworks/${a.id}`)
    } catch { setMessage('Could not save. Please sign in and check the API.') }
  }

  return <main className="mx-auto max-w-5xl px-6 py-16 sm:px-10">
    <p className="label-meta">Artist studio</p><h1 className="mt-3 text-5xl">Catalogue a new work.</h1>
    <p className="mt-4 max-w-2xl text-ink/60">Create a precise museum catalogue record for your work.</p>
    <form onSubmit={submit} className="mt-12 grid gap-8 border-t border-ink/15 pt-10 md:grid-cols-2">
      <Field label="Title" value={title} setValue={setTitle} required />
      <Field label="Image URL" value={imageUrl} setValue={setImageUrl} />
      <Field label="Style" value={style} setValue={setStyle} /><Field label="Medium" value={medium} setValue={setMedium} />
      <Field label="Year" value={year} setValue={setYear} /><Field label="Price (INR)" value={price} setValue={setPrice} />
      <label className="text-sm">Category<select value={category} onChange={e=>setCategory(e.target.value)} className="field mt-2"><option>Painting</option><option>Sculpture</option><option>Photography</option><option>Digital</option><option>Printmaking</option><option>Mixed Media</option></select></label>
      <label className="text-sm md:col-span-2">Description<textarea rows={7} value={description} onChange={e=>setDescription(e.target.value)} className="mt-2 w-full border border-ink/20 bg-transparent p-4 outline-none focus:border-oxblood" /></label>
      <div className="md:col-span-2 flex items-center justify-between border-t border-ink/15 pt-8"><span className="text-sm text-oxblood">{message}</span><button className="bg-oxblood px-8 py-4 text-sm uppercase tracking-widest text-paper hover:bg-ink">Save to archive</button></div>
    </form>
  </main>
}
function Field({label,value,setValue,required=false}:{label:string,value:string,setValue:(v:string)=>void,required?:boolean}) { return <label className="text-sm">{label}<input required={required} value={value} onChange={e=>setValue(e.target.value)} className="field mt-2" /></label> }
