'use client'

import { useEffect, useMemo, useState } from 'react'
import { supabase } from '@/lib/supabase'
import { ArrowLeft, Plus, Check, X, Edit, Trash2, Users } from 'lucide-react'

type Department = { id: string; name: string; code: string }
type Teacher = { id: string; full_name: string; email: string; department_id: string }
// courses.id can be bigint in DB, allow number|string
type Course = { id: number | string; name: string; code: string | null; department_id: string; weekly_hours?: number }
type TeacherCourse = { id: string; teacher_id: string; course_id: string; courses: { name: string; code: string | null } }
type Class = { id: string; name: string; department_id: string; academic_year: string; grade_level?: number }
type CourseClassAssignment = {
  id: string
  course_id: string
  class_id: string
  academic_year: string
  semester: string
  classes?: Class
}

export default function CoursesManagementPage() {
  const [departments, setDepartments] = useState<Department[]>([])
  const [selectedDeptId, setSelectedDeptId] = useState<string>('')
  const [teachers, setTeachers] = useState<Teacher[]>([])
  const [courses, setCourses] = useState<Course[]>([])
  const [assignments, setAssignments] = useState<Record<string, TeacherCourse[]>>({})
  const [loading, setLoading] = useState(true)
  const [creatingCourse, setCreatingCourse] = useState<number | null>(null) // Hangi sınıf seviyesine ders ekleniyor
  const [editingCourse, setEditingCourse] = useState<Course | null>(null)
  const [newCourse, setNewCourse] = useState({ name: '', code: '', gradeLevel: null as number | null, weeklyHours: 2 })
  
  // Sınıf atamaları için state'ler
  const [academicYear, setAcademicYear] = useState('2024-2025')
  const [semester, setSemester] = useState('Güz')
  const [allClasses, setAllClasses] = useState<Class[]>([])
  const [courseAssignments, setCourseAssignments] = useState<Record<string, CourseClassAssignment[]>>({})
  const [expandedCourse, setExpandedCourse] = useState<string | null>(null)
  const [assigningToGrade, setAssigningToGrade] = useState<{ courseId: string; gradeLevel: number } | null>(null)
  
  // Dinamik veriler
  const [availableAcademicYears, setAvailableAcademicYears] = useState<string[]>([])
  const [availableSemesters] = useState(['Güz', 'Bahar', 'Yaz']) // Dönemler sabit

  useEffect(() => {
    loadDepartments()
  }, [])

  useEffect(() => {
    loadAcademicYears()
  }, [])

  useEffect(() => {
    if (selectedDeptId) {
      loadTeachersAndCourses(selectedDeptId)
      loadClassesAndAssignments(selectedDeptId)
    }
  }, [selectedDeptId, academicYear, semester])

  async function loadDepartments() {
    setLoading(true)
    const { data } = await supabase.from('departments').select('*').order('name')
    setDepartments(data || [])
    setLoading(false)
  }

  async function loadAcademicYears() {
    try {
      // Veritabanındaki tüm akademik yılları çek
      const { data: classesData } = await supabase
        .from('classes')
        .select('academic_year')
        .order('academic_year', { ascending: false })

      if (classesData && classesData.length > 0) {
        // Unique akademik yılları al
        const uniqueYears = Array.from(new Set(classesData.map(c => c.academic_year).filter(Boolean)))
        setAvailableAcademicYears(uniqueYears.length > 0 ? uniqueYears : ['2024-2025'])
        
        // Eğer seçili akademik yıl listede yoksa, ilkini seç
        if (!uniqueYears.includes(academicYear) && uniqueYears.length > 0) {
          setAcademicYear(uniqueYears[0])
        }
      } else {
        // Varsayılan akademik yıllar
        const currentYear = new Date().getFullYear()
        const defaultYears = [
          `${currentYear}-${currentYear + 1}`,
          `${currentYear + 1}-${currentYear + 2}`,
          `${currentYear + 2}-${currentYear + 3}`
        ]
        setAvailableAcademicYears(defaultYears)
      }
    } catch (error: any) {
      console.error('Akademik yıllar yüklenirken hata:', error)
      // Hata durumunda varsayılan değerler
      const currentYear = new Date().getFullYear()
      setAvailableAcademicYears([
        `${currentYear}-${currentYear + 1}`,
        `${currentYear + 1}-${currentYear + 2}`,
        `${currentYear + 2}-${currentYear + 3}`
      ])
    }
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
        .select('id, name, code, department_id, weekly_hours')
        .eq('department_id', departmentId)
        .order('code'),
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
    
    // Dersler yüklendikten sonra sınıf atamalarını yükle
    if (c && c.length > 0) {
      await loadClassesAndAssignments(departmentId, c)
    }
  }

  async function loadClassesAndAssignments(departmentId: string, coursesList?: Course[]) {
    try {
      // courses state'ini kullan, eğer coursesList verilmişse onu kullan
      const coursesToUse = coursesList || courses
      
      // Sınıfları yükle
      const { data: classesData } = await supabase
        .from('classes')
        .select('*')
        .eq('department_id', departmentId)
        .eq('academic_year', academicYear)
        .order('grade_level')
        .order('name')

      // Grade level yoksa isimden çıkar
      const classesWithGrade = (classesData || []).map(cls => {
        if (!cls.grade_level && cls.name) {
          const match = cls.name.match(/^(\d+)/)
          if (match) {
            cls.grade_level = parseInt(match[1])
          }
        }
        return cls
      })
      setAllClasses(classesWithGrade)

      // Ders-sınıf atamalarını yükle
      const courseIds = coursesToUse.map(c => c.id.toString())
      if (courseIds.length > 0) {
        const classIds = classesWithGrade.map(c => c.id)
        const { data: assignmentsData } = await supabase
          .from('course_class_assignments')
          .select(`
            *,
            classes (id, name, academic_year)
          `)
          .in('course_id', courseIds)
          .in('class_id', classIds)
          .eq('academic_year', academicYear)
          .eq('semester', semester)

        // Ders bazında grupla
        const grouped: Record<string, CourseClassAssignment[]> = {}
        ;(assignmentsData || []).forEach((assignment: any) => {
          const courseId = assignment.course_id.toString()
          grouped[courseId] = grouped[courseId] || []
          grouped[courseId].push(assignment)
        })
        setCourseAssignments(grouped)
      } else {
        setCourseAssignments({})
      }
    } catch (error: any) {
      console.error('Sınıf ve atama yükleme hatası:', error)
    }
  }

  async function addCourse(departmentId: string, gradeLevel: number) {
    if (!newCourse.name.trim()) return
    if (!gradeLevel) return
    
    // Önce dersi ekle
    const { data: courseData, error: courseError } = await supabase
      .from('courses')
      .insert([
        { 
          name: newCourse.name.trim(), 
          code: newCourse.code.trim() || null, 
          department_id: departmentId,
          weekly_hours: newCourse.weeklyHours || 2
        },
      ])
      .select()
      .single()
    
    if (courseError) {
      alert('Ders eklenemedi: ' + courseError.message)
      return
    }

    // Seçilen sınıf seviyesindeki tüm sınıflara otomatik atama yap
    if (courseData) {
      try {
        // Önce sınıfları yükle (her zaman yeniden yükle, state güncel olmayabilir)
        await loadClassesAndAssignments(departmentId)
        
        // EN BASİT YÖNTEM: Sadece seviye ve bölüm ile filtrele, akademik yıl filtresini kaldır
        // Tüm sınıfları çek (akademik yıl filtresi YOK)
        const { data: allDeptClasses, error: classesError } = await supabase
          .from('classes')
          .select('*')
          .eq('department_id', departmentId)
          .order('academic_year', { ascending: false })
          .order('name')
        
        if (classesError) {
          alert(`Sınıflar yüklenirken hata: ${classesError.message}`)
          return
        }
        
        if (!allDeptClasses || allDeptClasses.length === 0) {
          alert(`Bu bölümde hiç sınıf bulunamadı. Önce sınıfları oluşturun.`)
          return
        }
        
        // Grade level'i isimden çıkar (eğer yoksa)
        const classesWithLevel = allDeptClasses.map(cls => {
          let level = cls.grade_level
          if (!level && cls.name) {
            // "1-A", "2-B" formatından seviyeyi çıkar
            const match = cls.name.match(/^(\d+)/)
            if (match) {
              level = parseInt(match[1])
            }
          }
          return { ...cls, grade_level: level }
        })
        
        const classesByLevel = classesWithLevel.reduce((acc, cls) => {
          const level = cls.grade_level || 0
          if (!acc[level]) acc[level] = []
          acc[level].push(cls)
          return acc
        }, {} as Record<number, Class[]>)
        
        // SADECE seviye ve bölüm ile filtrele (akademik yıl YOK)
        const targetClasses = classesWithLevel.filter(cls => {
          const level = cls.grade_level
          const levelMatch = level === gradeLevel
          const departmentMatch = String(cls.department_id) === String(departmentId)
          return levelMatch && departmentMatch
        })
        
        if (targetClasses.length === 0) {
          alert(`${gradeLevel}. sınıf seviyesi için bu bölümde sınıf bulunamadı. Mevcut seviyeler: ${Object.keys(classesByLevel).join(', ')}`)
          return
        }
        
        // Akademik yıl uyarısı (ama engelleme)
        const academicYears = [...new Set(targetClasses.map(c => c.academic_year))]
        if (academicYears.length > 1 || (academicYears.length === 1 && academicYears[0] !== academicYear)) {
          // Uyarı ver ama devam et
        }
        
        const finalTargetClasses = targetClasses

        if (finalTargetClasses.length > 0) {
          // Tüm sınıflara atama yap
          // course_id tipini kontrol et - UUID veya BIGINT olabilir
          const courseIdForAssignment = typeof courseData.id === 'string' 
            ? courseData.id 
            : courseData.id.toString()
          
          const assignments = finalTargetClasses.map(cls => ({
            course_id: courseIdForAssignment,
            class_id: cls.id,
            teacher_id: null,
            academic_year: academicYear,
            semester: semester
          }))
          
          const { error: assignError, data: insertedAssignments } = await supabase
            .from('course_class_assignments')
            .insert(assignments)
            .select(`
              *,
              classes (id, name, academic_year)
            `)

          if (assignError) {
            alert(`Ders eklendi ancak sınıf atamaları yapılamadı: ${assignError.message}`)
          } else {
            alert(`Ders başarıyla eklendi ve ${finalTargetClasses.length} sınıfa atandı`)
            
            // courseAssignments state'ini manuel olarak güncelle
            if (insertedAssignments && insertedAssignments.length > 0) {
              const courseId = courseData.id.toString()
              setCourseAssignments(prev => {
                const existing = prev[courseId] || []
                return {
                  ...prev,
                  [courseId]: [...existing, ...insertedAssignments]
                }
              })
            }
          }
        } else {
          alert(`Ders eklendi ancak ${gradeLevel}. sınıf seviyesi için bu akademik yılda (${academicYear}) sınıf bulunamadı. Önce sınıfları oluşturun.`)
        }
      } catch (error: any) {
        alert('Ders eklendi ancak sınıf atamaları yapılamadı')
      }
    }

    setNewCourse({ name: '', code: '', gradeLevel: null })
    setCreatingCourse(null)
    
    // Yeni eklenen dersi courses listesine ekle
    if (courseData) {
      const newCourseItem: Course = {
        id: courseData.id,
        name: courseData.name,
        code: courseData.code,
        department_id: courseData.department_id
      }
      
      // courses state'ini güncelle
      const updatedCourses = [...courses, newCourseItem]
      setCourses(updatedCourses)
      
      // Atamaları yeniden yükle (yeni ders dahil)
      if (selectedDeptId) {
        // Tüm dersleri yeniden yükle ve atamaları güncelle
        await loadTeachersAndCourses(departmentId)
        await loadClassesAndAssignments(selectedDeptId)
      }
    } else {
      // Dersler ve atamaları yeniden yükle
      await loadTeachersAndCourses(departmentId)
    }
  }

  async function updateCourse(courseId: number | string, departmentId: string) {
    if (!newCourse.name.trim()) return
    const { error } = await supabase
      .from('courses')
      .update({ 
        name: newCourse.name.trim(), 
        code: newCourse.code.trim() || null,
        weekly_hours: newCourse.weeklyHours || 2
      })
      .eq('id', courseId)
    if (error) {
      alert('Ders güncellenemedi: ' + error.message)
      return
    }
    setNewCourse({ name: '', code: '', gradeLevel: null, weeklyHours: 2 })
    setEditingCourse(null)
    setCreatingCourse(false)
    await loadTeachersAndCourses(departmentId)
  }

  function handleEdit(course: Course) {
    setEditingCourse(course)
    setNewCourse({ 
      name: course.name, 
      code: course.code || '',
      weeklyHours: course.weekly_hours || 2
    })
    setCreatingCourse(false)
  }

  function handleCancelEdit() {
    setEditingCourse(null)
    setNewCourse({ name: '', code: '', gradeLevel: null, weeklyHours: 2 })
    setCreatingCourse(null)
  }

  function startCreatingCourse(gradeLevel: number) {
    setCreatingCourse(gradeLevel)
    setNewCourse({ name: '', code: '', gradeLevel })
    setEditingCourse(null)
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
      
      if (selectedDeptId) {
        await loadTeachersAndCourses(selectedDeptId)
      }
    } catch (error: any) {
      console.error('Ders silinirken hata:', error)
      alert('Hata: ' + error.message)
    }
  }

  async function assignCourseToGradeLevel(courseId: string, gradeLevel: number) {
    try {
      // Seçilen seviyedeki tüm sınıfları bul
      const targetClasses = allClasses.filter(cls => {
        let level: number | null = cls.grade_level
        if (!level && cls.name) {
          const match = cls.name.match(/^(\d+)/)
          if (match) {
            level = parseInt(match[1])
          }
        }
        return level === gradeLevel
      })

      if (targetClasses.length === 0) {
        alert('Seçilen sınıf seviyesi için sınıf bulunamadı')
        return
      }

      // Tüm sınıflara atama yap
      const assignments = targetClasses.map(cls => ({
        course_id: courseId,
        class_id: cls.id,
        teacher_id: null,
        academic_year: academicYear,
        semester: semester
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
      setAssigningToGrade(null)
      if (selectedDeptId) await loadClassesAndAssignments(selectedDeptId, courses)
    } catch (error: any) {
      alert('Hata: ' + error.message)
    }
  }

  async function removeCourseAssignment(courseId: string, gradeLevel: number) {
    if (!confirm('Bu dersi tüm sınıflardan kaldırmak istediğinize emin misiniz?')) return

    try {
      const classesForLevel = allClasses.filter(cls => {
        let level: number | null = cls.grade_level
        if (!level && cls.name) {
          const match = cls.name.match(/^(\d+)/)
          if (match) {
            level = parseInt(match[1])
          }
        }
        return level === gradeLevel
      })
      const classIdsForLevel = classesForLevel.map(c => c.id)

      const { error } = await supabase
        .from('course_class_assignments')
        .delete()
        .eq('course_id', courseId)
        .eq('academic_year', academicYear)
        .eq('semester', semester)
        .in('class_id', classIdsForLevel)

      if (error) throw error
      alert('Ders tüm sınıflardan kaldırıldı')
      if (selectedDeptId) await loadClassesAndAssignments(selectedDeptId, courses)
    } catch (error: any) {
      alert('Hata: ' + error.message)
    }
  }

  function getGradeLevels(): number[] {
    // Eğer sınıflar yüklenmemişse, varsayılan olarak 1-4 göster (kartlar görünsün)
    if (allClasses.length === 0) {
      return [1, 2, 3, 4]
    }
    
    const levels = new Set<number>()
    allClasses.forEach(cls => {
      let level: number | null = cls.grade_level
      if (!level && cls.name) {
        const match = cls.name.match(/^(\d+)/)
        if (match) {
          level = parseInt(match[1])
        }
      }
      if (level) levels.add(level)
    })
    
    // Eğer hiç seviye bulunamadıysa, varsayılan olarak 1-4 göster
    if (levels.size === 0) {
      return [1, 2, 3, 4]
    }
    
    return Array.from(levels).sort()
  }

  function getAssignedGradeLevels(courseId: string): number[] {
    const assignments = courseAssignments[courseId.toString()] || []
    const levels = new Set<number>()
    assignments.forEach(assignment => {
      const cls = assignment.classes
      if (cls) {
        let level: number | null = cls.grade_level
        if (!level && cls.name) {
          const match = cls.name.match(/^(\d+)/)
          if (match) {
            level = parseInt(match[1])
          }
        }
        if (level) levels.add(level)
      }
    })
    return Array.from(levels).sort()
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
              <>
                <div>
                  <label className="block text-sm font-medium text-academic-text-primary mb-1">Akademik Yıl</label>
                  <select
                    value={academicYear}
                    onChange={e => setAcademicYear(e.target.value)}
                    className="input-field"
                  >
                    {availableAcademicYears.map(year => (
                      <option key={year} value={year}>{year}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-academic-text-primary mb-1">Dönem</label>
                  <select
                    value={semester}
                    onChange={e => setSemester(e.target.value)}
                    className="input-field"
                  >
                    {availableSemesters.map(sem => (
                      <option key={sem} value={sem}>{sem}</option>
                    ))}
                  </select>
                </div>
              </>
            )}
            {selectedDeptId && editingCourse && (
              <div className="flex-1">
                <div className="flex items-end space-x-2 flex-wrap gap-2">
                  <div className="flex-1 min-w-[200px]">
                    <label className="block text-sm font-medium text-academic-text-primary mb-1">Ders Adı</label>
                    <input
                      className="input-field"
                      value={newCourse.name}
                      onChange={e => setNewCourse({ ...newCourse, name: e.target.value })}
                      placeholder="Veritabanı I"
                    />
                  </div>
                  <div className="min-w-[120px]">
                    <label className="block text-sm font-medium text-academic-text-primary mb-1">Kod</label>
                    <input
                      className="input-field"
                      value={newCourse.code}
                      onChange={e => setNewCourse({ ...newCourse, code: e.target.value })}
                      placeholder="CSE101"
                    />
                  </div>
                  <div className="min-w-[120px]">
                    <label className="block text-sm font-medium text-academic-text-primary mb-1">Haftalık Saat</label>
                    <input
                      type="number"
                      min="1"
                      max="10"
                      className="input-field"
                      value={newCourse.weeklyHours}
                      onChange={e => setNewCourse({ ...newCourse, weeklyHours: parseInt(e.target.value) || 2 })}
                      placeholder="2"
                    />
                  </div>
                  <button 
                    className="btn-primary" 
                    onClick={() => updateCourse(editingCourse.id, selectedDeptId)}
                  >
                    <Check className="w-4 h-4" />
                  </button>
                  <button className="btn-secondary" onClick={handleCancelEdit}>
                    <X className="w-4 h-4" />
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>

        {selectedDept && (
          <div className="space-y-6">
            {/* Sınıf Seviyesi Bazlı Ders Ekleme */}
            {(() => {
              const gradeLevels = getGradeLevels()
              return (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                  {gradeLevels.map(level => {
                // Bu seviyedeki dersleri bul - courseAssignments'ten direkt kontrol et
                const coursesForLevel = courses.filter(c => {
                  const courseId = c.id.toString()
                  const assignments = courseAssignments[courseId] || []
                  
                  // Bu seviyedeki sınıfları bul
                  const classesForLevel = allClasses.filter(cls => {
                    let clsLevel: number | null = cls.grade_level
                    if (!clsLevel && cls.name) {
                      const match = cls.name.match(/^(\d+)/)
                      if (match) {
                        clsLevel = parseInt(match[1])
                      }
                    }
                    return clsLevel === level
                  })
                  const classIdsForLevel = classesForLevel.map(cl => cl.id)
                  
                  // Bu ders bu seviyedeki sınıflardan birine atanmış mı?
                  return assignments.some(assignment => 
                    classIdsForLevel.includes(assignment.class_id) &&
                    assignment.academic_year === academicYear &&
                    assignment.semester === semester
                  )
                })
                const isCreating = creatingCourse === level
                
                return (
                  <div key={level} className="card">
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="text-lg font-semibold text-academic-text-primary">
                        {level}. Sınıf Dersleri
                      </h3>
                      {!isCreating && (
                        <button
                          onClick={() => startCreatingCourse(level)}
                          className="btn-primary text-sm"
                        >
                          <Plus className="w-4 h-4 inline mr-1" />
                          Ders Ekle
                        </button>
                      )}
                    </div>

                    {/* Ders Ekleme Formu */}
                    {isCreating && (
                      <div className="mb-4 p-3 bg-academic-background rounded-md border border-academic-divider">
                        <div className="space-y-2">
                          <input
                            className="input-field text-sm"
                            value={newCourse.name}
                            onChange={e => setNewCourse({ ...newCourse, name: e.target.value })}
                            placeholder="Ders Adı"
                          />
                          <input
                            className="input-field text-sm"
                            value={newCourse.code}
                            onChange={e => setNewCourse({ ...newCourse, code: e.target.value })}
                            placeholder="Ders Kodu"
                          />
                          <div>
                            <label className="block text-xs font-medium text-academic-text-secondary mb-1">
                              Haftalık Saat
                            </label>
                            <input
                              type="number"
                              min="1"
                              max="10"
                              className="input-field text-sm"
                              value={newCourse.weeklyHours}
                              onChange={e => setNewCourse({ ...newCourse, weeklyHours: parseInt(e.target.value) || 2 })}
                              placeholder="2"
                            />
                            <p className="text-xs text-academic-text-secondary mt-1">
                              Bu ders haftada kaç saat işleniyor? (1-10 arası)
                            </p>
                          </div>
                          <div className="flex space-x-2">
                            <button
                              onClick={() => addCourse(selectedDeptId, level)}
                              className="btn-primary text-sm flex-1"
                            >
                              <Check className="w-4 h-4 inline mr-1" />
                              Ekle
                            </button>
                            <button
                              onClick={handleCancelEdit}
                              className="btn-secondary text-sm"
                            >
                              <X className="w-4 h-4" />
                            </button>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Bu seviyedeki dersler */}
                    {coursesForLevel.length === 0 ? (
                      <p className="text-academic-text-secondary text-sm">
                        Henüz ders eklenmedi
                      </p>
                    ) : (
                      <div className="space-y-2">
                        {coursesForLevel.map(c => {
                          const isExpanded = expandedCourse === c.id.toString()
                          return (
                            <div key={c.id}>
                              <div className="flex items-center justify-between p-2 bg-academic-surface rounded border border-academic-divider">
                                <div className="flex-1 min-w-0">
                                  <div className="text-sm font-medium text-academic-text-primary truncate">
                                    {c.name}
                                  </div>
                                  {c.code && (
                                    <div className="text-xs text-academic-text-secondary">
                                      {c.code}
                                    </div>
                                  )}
                                </div>
                                <div className="flex items-center space-x-1">
                                  <button
                                    onClick={() => setExpandedCourse(isExpanded ? null : c.id.toString())}
                                    className="p-1 text-academic-primary hover:bg-academic-primary-light rounded transition-colors"
                                    title="Detay"
                                  >
                                    <Users className="w-3 h-3" />
                                  </button>
                                  <button
                                    onClick={() => handleEdit(c)}
                                    className="p-1 text-academic-primary hover:bg-academic-primary-light rounded transition-colors"
                                    title="Düzenle"
                                  >
                                    <Edit className="w-3 h-3" />
                                  </button>
                                  <button
                                    onClick={() => handleDelete(c.id)}
                                    className="p-1 text-academic-error hover:bg-academic-error-light rounded transition-colors"
                                    title="Sil"
                                  >
                                    <Trash2 className="w-3 h-3" />
                                  </button>
                                </div>
                              </div>
                              
                              {/* Ders detayları */}
                              {isExpanded && (
                                <div className="mt-1 p-2 bg-academic-background rounded text-xs">
                                  <div className="text-academic-text-secondary">
                                    {academicYear} - {semester}
                                  </div>
                                </div>
                              )}
                            </div>
                          )
                        })}
                      </div>
                    )}
                  </div>
                )
              })}
                </div>
              )
            })()}

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



