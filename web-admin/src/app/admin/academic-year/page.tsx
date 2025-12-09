'use client'

import { useState, useEffect } from 'react'
import { supabase } from '@/lib/supabase'

type Department = { id: string; name: string; code: string }
type Class = { id: string; name: string; department_id: string; academic_year: string }
type Student = { id: string; full_name: string; email: string; class_id: string; student_number: string }

export default function AcademicYearPage() {
  const [departments, setDepartments] = useState<Department[]>([])
  const [selectedDeptId, setSelectedDeptId] = useState<string>('')
  const [currentYear, setCurrentYear] = useState<string>('2024-2025')
  const [newYear, setNewYear] = useState<string>('2025-2026')
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState(false)
  const [preview, setPreview] = useState<{ class: string; students: number; newClass: string }[]>([])

  useEffect(() => {
    loadData()
  }, [])

  useEffect(() => {
    if (selectedDeptId) {
      loadPreview()
    }
  }, [selectedDeptId, newYear])

  async function loadData() {
    try {
      const { data: depts } = await supabase
        .from('departments')
        .select('*')
        .order('name')

      setDepartments(depts || [])

      // Get current academic year from classes
      const { data: classes } = await supabase
        .from('classes')
        .select('academic_year')
        .order('academic_year', { ascending: false })
        .limit(1)

      if (classes && classes.length > 0) {
        setCurrentYear(classes[0].academic_year)
        // Auto-suggest next year
        const [start, end] = classes[0].academic_year.split('-')
        setNewYear(`${parseInt(end)}-${parseInt(end) + 1}`)
      }

      if (depts && depts.length > 0) {
        setSelectedDeptId(depts[0].id)
      }
    } catch (error: any) {
      alert('Hata: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  async function loadPreview() {
    if (!selectedDeptId || !newYear) return

    try {
      // Get current year classes
      const { data: classes } = await supabase
        .from('classes')
        .select('id, name')
        .eq('department_id', selectedDeptId)
        .eq('academic_year', currentYear)
        .order('name')

      if (!classes) return

      const previewData = await Promise.all(
        classes.map(async (cls) => {
          // Count students in this class
          const { count } = await supabase
            .from('students')
            .select('*', { count: 'exact', head: true })
            .eq('class_id', cls.id)

          // Determine new class name (1-A -> 2-A, 2-A -> 3-A, etc.)
          const classMatch = cls.name.match(/^(\d+)-([A-Z])$/)
          let newClassName = cls.name
          if (classMatch) {
            const yearNum = parseInt(classMatch[1])
            const section = classMatch[2]
            if (yearNum < 4) {
              newClassName = `${yearNum + 1}-${section}`
            } else {
              newClassName = `Mezun`
            }
          }

          return {
            class: cls.name,
            students: count || 0,
            newClass: newClassName
          }
        })
      )

      setPreview(previewData)
    } catch (error: any) {
      console.error('Preview error:', error)
    }
  }

  async function handlePromoteStudents() {
    if (!confirm('Yıl sonu işlemini başlatmak istediğinize emin misiniz? Bu işlem geri alınamaz!')) {
      return
    }

    setProcessing(true)

    try {
      // Get current year classes for selected department
      const { data: classes } = await supabase
        .from('classes')
        .select('id, name, department_id')
        .eq('department_id', selectedDeptId)
        .eq('academic_year', currentYear)

      if (!classes || classes.length === 0) {
        alert('Bu bölüm için mevcut yılda sınıf bulunamadı')
        setProcessing(false)
        return
      }

      // Create new classes for new academic year
      const newClasses: { [key: string]: string } = {} // old class name -> new class id

      for (const cls of classes) {
        const classMatch = cls.name.match(/^(\d+)-([A-Z])$/)
        let newClassName = cls.name

        if (classMatch) {
          const yearNum = parseInt(classMatch[1])
          const section = classMatch[2]
          if (yearNum < 4) {
            newClassName = `${yearNum + 1}-${section}`
          } else {
            newClassName = `Mezun`
          }
        }

        // Check if new class already exists
        let { data: existingClass } = await supabase
          .from('classes')
          .select('id')
          .eq('department_id', cls.department_id)
          .eq('name', newClassName)
          .eq('academic_year', newYear)
          .single()

        if (!existingClass) {
          // Create new class
          const { data: newClass, error } = await supabase
            .from('classes')
            .insert({
              name: newClassName,
              department_id: cls.department_id,
              academic_year: newYear
            })
            .select('id')
            .single()

          if (error) throw error
          existingClass = newClass
        }

        newClasses[cls.id] = existingClass.id
      }

      // Update students' class_id
      for (const [oldClassId, newClassId] of Object.entries(newClasses)) {
        const { error } = await supabase
          .from('students')
          .update({ class_id: newClassId })
          .eq('class_id', oldClassId)

        if (error) throw error
      }

      alert('Yıl sonu işlemi başarıyla tamamlandı!')
      await loadData()
      await loadPreview()
    } catch (error: any) {
      alert('Hata: ' + error.message)
      console.error('Promotion error:', error)
    } finally {
      setProcessing(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-academic-background flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-academic-primary mx-auto"></div>
          <p className="mt-4 text-academic-text-secondary">Yükleniyor...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-academic-background p-6">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-academic-text-primary mb-6">Yıl Sonu İşlemleri</h1>

        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold text-academic-text-primary mb-4">Akademik Yıl Bilgileri</h2>
          
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-2">
                Mevcut Akademik Yıl
              </label>
              <input
                type="text"
                value={currentYear}
                readOnly
                className="w-full px-4 py-2 border border-academic-border rounded-lg bg-gray-100"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-2">
                Yeni Akademik Yıl
              </label>
              <input
                type="text"
                value={newYear}
                onChange={(e) => setNewYear(e.target.value)}
                className="w-full px-4 py-2 border border-academic-border rounded-lg focus:ring-2 focus:ring-academic-primary"
                placeholder="2025-2026"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-2">
                Bölüm
              </label>
              <select
                value={selectedDeptId}
                onChange={(e) => setSelectedDeptId(e.target.value)}
                className="w-full px-4 py-2 border border-academic-border rounded-lg focus:ring-2 focus:ring-academic-primary"
              >
                {departments.map((dept) => (
                  <option key={dept.id} value={dept.id}>
                    {dept.name}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Preview */}
        {preview.length > 0 && (
          <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            <h2 className="text-xl font-semibold text-academic-text-primary mb-4">Önizleme</h2>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-academic-background">
                  <tr>
                    <th className="px-4 py-2 text-left text-xs font-medium text-academic-text-secondary uppercase">
                      Mevcut Sınıf
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-academic-text-secondary uppercase">
                      Öğrenci Sayısı
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-academic-text-secondary uppercase">
                      Yeni Sınıf
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-academic-border">
                  {preview.map((item, idx) => (
                    <tr key={idx}>
                      <td className="px-4 py-2 text-sm text-academic-text-primary">{item.class}</td>
                      <td className="px-4 py-2 text-sm text-academic-text-primary">{item.students}</td>
                      <td className="px-4 py-2 text-sm text-academic-text-primary font-semibold">
                        {item.newClass}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Action Button */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-4">
            <p className="text-sm text-yellow-800">
              <strong>Uyarı:</strong> Bu işlem öğrencilerin sınıflarını otomatik olarak yükseltecek ve yeni akademik yıl için sınıflar oluşturacaktır. 
              Bu işlem geri alınamaz. Lütfen önizlemeyi kontrol edin.
            </p>
          </div>

          <button
            onClick={handlePromoteStudents}
            disabled={processing || !newYear || !selectedDeptId}
            className="w-full bg-academic-primary text-white px-6 py-3 rounded-lg hover:bg-academic-primary-dark transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {processing ? 'İşleniyor...' : 'Yıl Sonu İşlemini Başlat'}
          </button>
        </div>
      </div>
    </div>
  )
}

