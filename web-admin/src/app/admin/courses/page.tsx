'use client'

import { useEffect, useMemo, useState } from 'react'
import { supabase } from '@/lib/supabase'
import { ArrowLeft, Plus, Check, X, Edit, Trash2 } from 'lucide-react'

type Department = { id: string; name: string; code: string }
type Teacher = { id: string; full_name: string; email: string; department_id: string }
// courses.id can be bigint in DB, allow number|string
type Course = { id: number | string; name: string; code: string | null; department_id: string }
type TeacherCourse = { id: string; teacher_id: string; course_id: string; courses: { name: string; code: string | null } }

export default function CoursesManagementPage() {
  const [departments, setDepartments] = useState<Department[]>([])
  const [selectedDeptId, setSelectedDeptId] = useState<string>('')
  const [teachers, setTeachers] = useState<Teacher[]>([])
  const [courses, setCourses] = useState<Course[]>([])
  const [assignments, setAssignments] = useState<Record<string, TeacherCourse[]>>({})
  const [loading, setLoading] = useState(true)
  const [creatingCourse, setCreatingCourse] = useState(false)
  const [editingCourse, setEditingCourse] = useState<Course | null>(null)
  const [newCourse, setNewCourse] = useState({ name: '', code: '' })

  useEffect(() => {
    loadDepartments()
  }, [])

  useEffect(() => {
    if (selectedDeptId) {
      loadTeachersAndCourses(selectedDeptId)
    }
  }, [selectedDeptId])

  async function loadDepartments() {
    setLoading(true)
    const { data } = await supabase.from('departments').select('*').order('name')
    setDepartments(data || [])
    setLoading(false)
  }

  async function loadTeachersAndCourses(departmentId: string) {
    setLoading(true)
    const [{ data: t }, { data: c }] = await Promise.all([
      supabase
        .from('profiles')
        .select('id, full_name, email, department_id')
        .eq('role', 'teacher')
        .eq('department_id', departmentId)
        .order('full_name'),
      supabase
        .from('courses')
        .select('id, name, code, department_id')
        .eq('department_id', departmentId)
        .order('name'),
    ])
    setTeachers(t || [])
    setCourses(c || [])

    // load current assignments per teacher
    const teacherIds = (t || []).map(row => row.id)
    if (teacherIds.length > 0) {
      const { data: tc } = await supabase
        .from('teacher_courses')
        .select('id, teacher_id, course_id, courses(name, code)')
        .in('teacher_id', teacherIds)

      const grouped: Record<string, TeacherCourse[]> = {}
      ;(tc || []).forEach(row => {
        grouped[row.teacher_id] = grouped[row.teacher_id] || []
        grouped[row.teacher_id].push(row as unknown as TeacherCourse)
      })
      setAssignments(grouped)
    } else {
      setAssignments({})
    }
    setLoading(false)
  }

  async function addCourse(departmentId: string) {
    if (!newCourse.name.trim()) return
    const { error } = await supabase.from('courses').insert([
      { name: newCourse.name.trim(), code: newCourse.code.trim() || null, department_id: departmentId },
    ])
    if (error) {
      alert('Ders eklenemedi: ' + error.message)
      return
    }
    setNewCourse({ name: '', code: '' })
    setCreatingCourse(false)
    await loadTeachersAndCourses(departmentId)
  }

  async function updateCourse(courseId: number | string, departmentId: string) {
    if (!newCourse.name.trim()) return
    const { error } = await supabase
      .from('courses')
      .update({ name: newCourse.name.trim(), code: newCourse.code.trim() || null })
      .eq('id', courseId)
    if (error) {
      alert('Ders güncellenemedi: ' + error.message)
      return
    }
    setNewCourse({ name: '', code: '' })
    setEditingCourse(null)
    setCreatingCourse(false)
    await loadTeachersAndCourses(departmentId)
  }

  function handleEdit(course: Course) {
    setEditingCourse(course)
    setNewCourse({ name: course.name, code: course.code || '' })
    setCreatingCourse(false)
  }

  function handleCancelEdit() {
    setEditingCourse(null)
    setNewCourse({ name: '', code: '' })
    setCreatingCourse(false)
  }

  async function handleDelete(courseId: number | string) {
    if (!confirm('Bu dersi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz ve dersin tüm atamaları silinecektir.')) return

    try {
      // Önce teacher_courses'dan ilgili atamaları sil
      const { error: assignError } = await supabase
        .from('teacher_courses')
        .delete()
        .eq('course_id', courseId)
      
      if (assignError) {
        console.warn('Atamalar silinirken hata:', assignError)
      }

      // Sonra dersi sil
      const { error } = await supabase
        .from('courses')
        .delete()
        .eq('id', courseId)

      if (error) throw error
      
      if (selectedDeptId) await loadTeachersAndCourses(selectedDeptId)
    } catch (error: any) {
      console.error('Ders silinirken hata:', error)
      alert('Hata: ' + error.message)
    }
  }

  async function assignCourse(teacherId: string, courseId: string) {
    if (!courseId) return
    const { error } = await supabase.from('teacher_courses').insert([{ teacher_id: teacherId, course_id: courseId }])
    if (error) {
      if (error.message?.includes('duplicate')) {
        alert('Bu ders zaten atanmış')
      } else {
        alert('Atama yapılamadı: ' + error.message)
      }
      return
    }
    if (selectedDeptId) await loadTeachersAndCourses(selectedDeptId)
  }

  async function removeAssignment(id: string) {
    const { error } = await supabase.from('teacher_courses').delete().eq('id', id)
    if (error) {
      alert('Atama silinemedi: ' + error.message)
      return
    }
    if (selectedDeptId) await loadTeachersAndCourses(selectedDeptId)
  }

  const selectedDept = useMemo(() => departments.find(d => d.id === selectedDeptId), [departments, selectedDeptId])

  if (loading && departments.length === 0) {
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
    <div className="min-h-screen bg-academic-background">
      {/* Header */}
      <header className="bg-academic-surface border-b border-academic-divider">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-4">
              <button onClick={() => (window.location.href = '/')} className="btn-outline flex items-center space-x-2">
                <ArrowLeft className="w-4 h-4" />
                <span>Geri</span>
              </button>
              <div>
                <h1 className="text-2xl font-bold text-academic-primary">Ders Yönetimi</h1>
                <p className="text-academic-text-secondary">Bölümler, öğretmenler ve ders atamaları</p>
              </div>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Department Selector */}
        <div className="card mb-6">
          <div className="flex flex-col md:flex-row md:items-end md:space-x-4 gap-3">
            <div className="flex-1">
              <label className="block text-sm font-medium text-academic-text-primary mb-1">Bölüm</label>
              <select
                value={selectedDeptId}
                onChange={e => setSelectedDeptId(e.target.value)}
                className="input-field"
              >
                <option value="">Bölüm seçin</option>
                {departments.map(d => (
                  <option key={d.id} value={d.id}>
                    {d.name} ({d.code})
                  </option>
                ))}
              </select>
            </div>
            {selectedDeptId && (
              <div className="flex-1">
                {!creatingCourse && !editingCourse ? (
                  <button className="btn-primary" onClick={() => setCreatingCourse(true)}>
                    <Plus className="w-4 h-4 inline mr-2" /> Yeni Ders Ekle
                  </button>
                ) : (
                  <div className="flex items-end space-x-2">
                    <div className="flex-1">
                      <label className="block text-sm font-medium text-academic-text-primary mb-1">Ders Adı</label>
                      <input
                        className="input-field"
                        value={newCourse.name}
                        onChange={e => setNewCourse({ ...newCourse, name: e.target.value })}
                        placeholder="Veritabanı I"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-academic-text-primary mb-1">Kod</label>
                      <input
                        className="input-field"
                        value={newCourse.code}
                        onChange={e => setNewCourse({ ...newCourse, code: e.target.value })}
                        placeholder="CSE101"
                      />
                    </div>
                    <button 
                      className="btn-primary" 
                      onClick={() => editingCourse ? updateCourse(editingCourse.id, selectedDeptId) : addCourse(selectedDeptId)}
                    >
                      <Check className="w-4 h-4" />
                    </button>
                    <button className="btn-secondary" onClick={handleCancelEdit}>
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {selectedDept && (
          <div className="space-y-6">
            {/* Courses list */}
            <div className="card">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-academic-text-primary">{selectedDept.name} Dersleri</h2>
              </div>
              {courses.length === 0 ? (
                <p className="text-academic-text-secondary">Bu bölümde henüz ders yok.</p>
              ) : (
                <div className="space-y-2">
                  {courses.map(c => (
                    <div 
                      key={c.id} 
                      className={`flex items-center justify-between p-3 rounded-md border ${
                        editingCourse?.id === c.id 
                          ? 'border-academic-primary bg-academic-primary-light' 
                          : 'border-academic-divider bg-academic-surface'
                      }`}
                    >
                      <div className="flex-1">
                        <span className="text-academic-text-primary font-medium">
                          {c.name}
                          {c.code && <span className="text-academic-text-secondary ml-2">({c.code})</span>}
                        </span>
                      </div>
                      <div className="flex items-center space-x-2">
                        <button
                          onClick={() => handleEdit(c)}
                          className="p-2 text-academic-primary hover:bg-academic-primary-light rounded-md transition-colors"
                          title="Düzenle"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(c.id)}
                          className="p-2 text-academic-error hover:bg-academic-error-light rounded-md transition-colors"
                          title="Sil"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Teachers and assignments */}
            <div className="card">
              <h2 className="text-lg font-semibold text-academic-text-primary mb-4">Öğretmenler ve Ders Atamaları</h2>
              {teachers.length === 0 ? (
                <p className="text-academic-text-secondary">Bu bölümde öğretmen bulunamadı.</p>
              ) : (
                <div className="space-y-4">
                  {teachers.map(t => {
                    const current = assignments[t.id] || []
                    return (
                      <div key={t.id} className="border border-academic-divider rounded-md p-4">
                        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
                          <div>
                            <div className="text-academic-text-primary font-medium">{t.full_name}</div>
                            <div className="text-academic-text-secondary text-sm">{t.email}</div>
                          </div>
                          <div className="flex-1 md:max-w-md">
                            <label className="block text-sm font-medium text-academic-text-primary mb-1">Ders Ata</label>
                            <div className="flex space-x-2">
                              <select className="input-field flex-1" defaultValue="" onChange={e => assignCourse(t.id, e.target.value)}>
                                <option value="">Ders seçin</option>
                                {courses.map(c => (
                                  <option key={c.id} value={c.id}>{c.name}{c.code ? ` (${c.code})` : ''}</option>
                                ))}
                              </select>
                            </div>
                          </div>
                        </div>
                        {current.length > 0 && (
                          <div className="mt-3 flex flex-wrap gap-2">
                            {current.map(a => (
                              <span key={a.id} className="px-2 py-1 rounded-full bg-academic-chip text-academic-text-primary text-sm flex items-center gap-2">
                                {a.courses?.name}{a.courses?.code ? ` (${a.courses.code})` : ''}
                                <button className="text-academic-error" onClick={() => removeAssignment(a.id)}>×</button>
                              </span>
                            ))}
                          </div>
                        )}
                      </div>
                    )
                  })}
                </div>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  )
}



