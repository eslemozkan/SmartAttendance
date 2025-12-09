'use client'

import { useState, useEffect } from 'react'
import { supabase } from '@/lib/supabase'

type Department = { id: string; name: string; code: string }
type Class = { id: string; name: string; department_id: string; academic_year: string; grade_level?: number }
type Course = { id: number | string; name: string; code: string | null; department_id: string }
type CourseClassAssignment = {
  id: string
  course_id: string
  class_id: string
  teacher_id: string | null
  academic_year: string
  semester: string
  courses?: Course
  classes?: Class
}

type GradeLevelCard = {
  level: number
  name: string
  assignments: CourseClassAssignment[]
  classes: Class[]
}

export default function CourseAssignmentsPage() {
  const [departments, setDepartments] = useState<Department[]>([])
  const [selectedDeptId, setSelectedDeptId] = useState<string>('')
  const [courses, setCourses] = useState<Course[]>([])
  const [allAssignments, setAllAssignments] = useState<CourseClassAssignment[]>([])
  const [allClasses, setAllClasses] = useState<Class[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [selectedGradeLevel, setSelectedGradeLevel] = useState<number | null>(null)
  const [formData, setFormData] = useState({
    course_id: '',
    academic_year: '2024-2025',
    semester: 'Güz'
  })

  const gradeLevels = [
    { level: 1, name: '1. Sınıf' },
    { level: 2, name: '2. Sınıf' },
    { level: 3, name: '3. Sınıf' },
    { level: 4, name: '4. Sınıf' }
  ]

  useEffect(() => {
    loadData()
  }, [])

  useEffect(() => {
    if (selectedDeptId) {
      loadClassesAndCourses(selectedDeptId)
      loadAssignments(selectedDeptId)
    }
  }, [selectedDeptId, formData.academic_year, formData.semester])

  async function loadData() {
    try {
      const { data: depts } = await supabase
        .from('departments')
        .select('*')
        .order('name')

      setDepartments(depts || [])
      if (depts && depts.length > 0) {
        setSelectedDeptId(depts[0].id)
      }
    } catch (error: any) {
      alert('Hata: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  async function loadClassesAndCourses(deptId: string) {
    try {
      // Dinamik olarak sınıfları ve dersleri yükle
      const [classesRes, coursesRes] = await Promise.all([
        supabase
          .from('classes')
          .select('*')
          .eq('department_id', deptId)
          .eq('academic_year', formData.academic_year)
          .order('grade_level')
          .order('name'),
        supabase
          .from('courses')
          .select('*')
          .eq('department_id', deptId)
          .order('code')
      ])

      // Eğer sınıflarda grade_level yoksa, isimden çıkar
      const classesWithGrade = (classesRes.data || []).map(cls => {
        if (!cls.grade_level && cls.name) {
          const match = cls.name.match(/^(\d+)/)
          if (match) {
            cls.grade_level = parseInt(match[1])
          }
        }
        return cls
      })

      setAllClasses(classesWithGrade)
      setCourses(coursesRes.data || [])
    } catch (error: any) {
      alert('Hata: ' + error.message)
    }
  }

  async function loadAssignments(deptId: string) {
    try {
      const { data: classIds } = await supabase
        .from('classes')
        .select('id')
        .eq('department_id', deptId)

      if (!classIds || classIds.length === 0) {
        setAllAssignments([])
        return
      }

      const classIdList = classIds.map(c => c.id)

      const { data, error } = await supabase
        .from('course_class_assignments')
        .select(`
          *,
          courses (id, name, code),
          classes (id, name, department_id, academic_year, grade_level)
        `)
        .in('class_id', classIdList)
        .order('academic_year', { ascending: false })
        .order('semester')

      if (error) throw error
      setAllAssignments(data || [])
    } catch (error: any) {
      alert('Hata: ' + error.message)
    }
  }

  function getGradeLevelCards(): GradeLevelCard[] {
    return gradeLevels.map(gl => {
      // Get classes for this grade level
      const classesForLevel = allClasses.filter(
        cls => cls.grade_level === gl.level && cls.academic_year === formData.academic_year
      )

      // Get unique courses assigned to these classes
      const classIdsForLevel = classesForLevel.map(c => c.id)
      const assignmentsForLevel = allAssignments.filter(
        assignment => 
          classIdsForLevel.includes(assignment.class_id) &&
          assignment.academic_year === formData.academic_year &&
          assignment.semester === formData.semester
      )

      // Get unique courses (by course_id)
      const uniqueCourses = new Map<string, CourseClassAssignment>()
      assignmentsForLevel.forEach(assignment => {
        const courseId = assignment.course_id
        if (!uniqueCourses.has(courseId)) {
          uniqueCourses.set(courseId, assignment)
        }
      })

      return {
        level: gl.level,
        name: gl.name,
        assignments: Array.from(uniqueCourses.values()),
        classes: classesForLevel
      }
    })
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!formData.course_id || !selectedGradeLevel) {
      alert('Lütfen ders ve sınıf seviyesi seçin')
      return
    }

    try {
      // Dinamik olarak sınıfları bul - grade_level varsa kullan, yoksa isimden çıkar
      const { data: allDeptClasses, error: fetchError } = await supabase
        .from('classes')
        .select('id, name, grade_level')
        .eq('department_id', selectedDeptId)
        .eq('academic_year', formData.academic_year)

      if (fetchError) throw fetchError

      // Grade level'a göre filtrele (dinamik)
      const targetClasses = (allDeptClasses || []).filter(cls => {
        let level: number | null = cls.grade_level
        
        // Eğer grade_level yoksa, isimden çıkar
        if (!level && cls.name) {
          const match = cls.name.match(/^(\d+)/)
          if (match) {
            level = parseInt(match[1])
          }
        }
        
        return level === selectedGradeLevel
      }).map(cls => ({ id: cls.id }))

      if (classesError) throw classesError

      if (!targetClasses || targetClasses.length === 0) {
        alert('Seçilen sınıf seviyesi için sınıf bulunamadı')
        return
      }

      // Create assignments for all classes in this grade level
      const assignments = targetClasses.map(cls => ({
        course_id: formData.course_id,
        class_id: cls.id,
        teacher_id: null,
        academic_year: formData.academic_year,
        semester: formData.semester
      }))

      const { error } = await supabase
        .from('course_class_assignments')
        .insert(assignments)

      if (error) {
        if (error.message?.includes('duplicate') || error.message?.includes('unique')) {
          alert('Bu atamalardan bazıları zaten mevcut')
        } else {
          alert('Atama yapılamadı: ' + error.message)
        }
        return
      }

      alert(`${targetClasses.length} sınıfa atama başarıyla yapıldı`)
      setShowForm(false)
      setSelectedGradeLevel(null)
      setFormData({
        course_id: '',
        academic_year: '2024-2025',
        semester: 'Güz'
      })
      if (selectedDeptId) loadAssignments(selectedDeptId)
    } catch (error: any) {
      alert('Hata: ' + error.message)
    }
  }

  async function handleDelete(assignmentId: string, gradeLevel: number) {
    if (!confirm('Bu dersi tüm sınıflardan kaldırmak istediğinize emin misiniz?')) return

    try {
      // Get the assignment to find course_id
      const assignment = allAssignments.find(a => a.id === assignmentId)
      if (!assignment) return

      // Get all classes for this grade level
      const classesForLevel = allClasses.filter(
        cls => cls.grade_level === gradeLevel && cls.academic_year === formData.academic_year
      )
      const classIdsForLevel = classesForLevel.map(c => c.id)

      // Delete all assignments for this course and grade level
      const { error } = await supabase
        .from('course_class_assignments')
        .delete()
        .eq('course_id', assignment.course_id)
        .eq('academic_year', formData.academic_year)
        .eq('semester', formData.semester)
        .in('class_id', classIdsForLevel)

      if (error) throw error
      alert('Ders tüm sınıflardan kaldırıldı')
      if (selectedDeptId) loadAssignments(selectedDeptId)
    } catch (error: any) {
      alert('Hata: ' + error.message)
    }
  }

  function openAddForm(gradeLevel: number) {
    setSelectedGradeLevel(gradeLevel)
    setShowForm(true)
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

  const gradeLevelCards = getGradeLevelCards()

  return (
    <div className="min-h-screen bg-academic-background p-6">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-3xl font-bold text-academic-text-primary mb-6">Ders-Sınıf Atamaları</h1>

        {/* Department and Year/Semester Selection */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-2">
                Bölüm Seçin
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

            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-2">
                Akademik Yıl
              </label>
              <input
                type="text"
                value={formData.academic_year}
                onChange={(e) => {
                  setFormData({ ...formData, academic_year: e.target.value })
                }}
                className="w-full px-4 py-2 border border-academic-border rounded-lg focus:ring-2 focus:ring-academic-primary"
                placeholder="2024-2025"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-2">
                Dönem
              </label>
              <select
                value={formData.semester}
                onChange={(e) => {
                  setFormData({ ...formData, semester: e.target.value })
                }}
                className="w-full px-4 py-2 border border-academic-border rounded-lg focus:ring-2 focus:ring-academic-primary"
              >
                <option value="Güz">Güz</option>
                <option value="Bahar">Bahar</option>
              </select>
            </div>
          </div>
        </div>

        {/* Add Course Form Modal */}
        {showForm && selectedGradeLevel && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
              <h2 className="text-xl font-semibold text-academic-text-primary mb-4">
                {gradeLevels.find(gl => gl.level === selectedGradeLevel)?.name} - Ders Ekle
              </h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-academic-text-primary mb-2">
                    Ders Seçin
                  </label>
                  <select
                    value={formData.course_id}
                    onChange={(e) => setFormData({ ...formData, course_id: e.target.value })}
                    className="w-full px-4 py-2 border border-academic-border rounded-lg focus:ring-2 focus:ring-academic-primary"
                    required
                  >
                    <option value="">Ders Seçin</option>
                    {courses.map((course) => (
                      <option key={course.id} value={course.id}>
                        {course.name} ({course.code})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="flex gap-2">
                  <button
                    type="submit"
                    className="flex-1 bg-academic-primary text-white px-6 py-2 rounded-lg hover:bg-academic-primary-dark transition"
                  >
                    Ekle
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowForm(false)
                      setSelectedGradeLevel(null)
                    }}
                    className="flex-1 bg-gray-300 text-gray-700 px-6 py-2 rounded-lg hover:bg-gray-400 transition"
                  >
                    İptal
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Grade Level Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {gradeLevelCards.map((card) => (
            <div key={card.level} className="bg-white rounded-lg shadow-md overflow-hidden">
              <div className="bg-academic-primary text-white px-6 py-4">
                <h2 className="text-xl font-bold">{card.name}</h2>
                <p className="text-sm text-academic-primary-light">
                  {card.classes.length} sınıf • {card.assignments.length} ders
                </p>
              </div>

              <div className="p-4">
                {/* Add Course Button */}
                <button
                  onClick={() => openAddForm(card.level)}
                  className="w-full bg-academic-primary-light text-academic-primary px-4 py-2 rounded-lg hover:bg-academic-primary-light-dark transition mb-4 font-medium"
                >
                  + Ders Ekle
                </button>

                {/* Courses List */}
                <div className="space-y-2 max-h-96 overflow-y-auto">
                  {card.assignments.length === 0 ? (
                    <p className="text-sm text-academic-text-secondary text-center py-4">
                      Henüz ders atanmamış
                    </p>
                  ) : (
                    card.assignments.map((assignment) => (
                      <div
                        key={assignment.id}
                        className="flex items-center justify-between p-3 bg-academic-background rounded-lg hover:bg-gray-100 transition"
                      >
                        <div className="flex-1">
                          <p className="text-sm font-medium text-academic-text-primary">
                            {assignment.courses?.name}
                          </p>
                          <p className="text-xs text-academic-text-secondary">
                            {assignment.courses?.code}
                          </p>
                        </div>
                        <button
                          onClick={() => handleDelete(assignment.id, card.level)}
                          className="text-red-600 hover:text-red-800 text-sm ml-2"
                          title="Dersi kaldır"
                        >
                          ✕
                        </button>
                      </div>
                    ))
                  )}
                </div>

                {/* Classes Info */}
                {card.classes.length > 0 && (
                  <div className="mt-4 pt-4 border-t border-academic-border">
                    <p className="text-xs text-academic-text-secondary mb-2">Sınıflar:</p>
                    <div className="flex flex-wrap gap-1">
                      {card.classes.map((cls) => (
                        <span
                          key={cls.id}
                          className="text-xs bg-gray-200 text-gray-700 px-2 py-1 rounded"
                        >
                          {cls.name}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
