'use client'

import { useEffect, useMemo, useState } from 'react'
import { supabase } from '@/lib/supabase'
import { ArrowLeft, Calendar, Download, Filter, TrendingUp, Users, CheckCircle } from 'lucide-react'

type Department = { id: string; name: string; code: string }
type Course = { id: number | string; name: string; code: string | null; department_id: string }
type Class = { id: string; name: string; department_id: string; academic_year: string }
type Attendance = {
  id: string
  course_id: number
  week_number: number
  student_id: string
  marked_at: string
  method: string
  profiles?: { full_name: string; email: string }
  courses?: { name: string; code: string | null }
  students?: { full_name: string; email: string; class_id: string }
  classes?: { name: string; academic_year: string }
}

export default function ReportsPage() {
  const [departments, setDepartments] = useState<Department[]>([])
  const [selectedDeptId, setSelectedDeptId] = useState<string>('')
  const [courses, setCourses] = useState<Course[]>([])
  const [classes, setClasses] = useState<Class[]>([])
  const [selectedCourseId, setSelectedCourseId] = useState<string>('')
  const [selectedClassId, setSelectedClassId] = useState<string>('')
  // Initialize with default date range (last 30 days)
  const getDefaultDateRange = () => {
    const today = new Date()
    const thirtyDaysAgo = new Date(today)
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
    return {
      from: thirtyDaysAgo.toISOString().split('T')[0],
      to: today.toISOString().split('T')[0]
    }
  }
  const defaultDates = getDefaultDateRange()
  const [dateFrom, setDateFrom] = useState<string>(defaultDates.from)
  const [dateTo, setDateTo] = useState<string>(defaultDates.to)
  const [attendances, setAttendances] = useState<Attendance[]>([])
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState({
    totalRecords: 0,
    uniqueStudents: 0,
    uniqueCourses: 0,
    attendanceRate: 0
  })

  useEffect(() => {
    loadDepartments()
  }, [])

  // Load attendances when component mounts and dates are ready
  useEffect(() => {
    // Only load if we have date range set
    if (dateFrom && dateTo) {
      loadAttendances()
    }
  }, []) // Run only once on mount

  useEffect(() => {
    if (selectedDeptId) {
      loadCoursesAndClasses(selectedDeptId)
    } else {
      setCourses([])
      setClasses([])
    }
  }, [selectedDeptId])

  useEffect(() => {
    // Load attendances when filters change (skip initial load to avoid double loading)
    if (dateFrom && dateTo) {
      loadAttendances()
    }
  }, [selectedDeptId, selectedCourseId, selectedClassId, dateFrom, dateTo])

  async function loadDepartments() {
    setLoading(true)
    const { data } = await supabase.from('departments').select('*').order('name')
    setDepartments(data || [])
    setLoading(false)
  }

  async function loadCoursesAndClasses(departmentId: string) {
    const [{ data: c }, { data: cls }] = await Promise.all([
      supabase
        .from('courses')
        .select('id, name, code, department_id')
        .eq('department_id', departmentId)
        .order('name'),
      supabase
        .from('classes')
        .select('id, name, department_id, academic_year')
        .eq('department_id', departmentId)
        .order('name'),
    ])
    setCourses(c || [])
    setClasses(cls || [])
  }

  async function loadAttendances() {
    setLoading(true)
    try {
      console.log('Loading attendances with filters:', {
        selectedDeptId,
        selectedCourseId,
        selectedClassId,
        dateFrom,
        dateTo,
        coursesCount: courses.length
      })

      // Try without joins first (more reliable)
      let query = supabase
        .from('attendances')
        .select('id, course_id, week_number, student_id, marked_at, method')
        .order('marked_at', { ascending: false })

      // Apply filters
      if (selectedCourseId) {
        query = query.eq('course_id', selectedCourseId)
      }

      if (dateFrom) {
        query = query.gte('marked_at', dateFrom + 'T00:00:00')
      }

      if (dateTo) {
        query = query.lte('marked_at', dateTo + 'T23:59:59')
      }

      // If department is selected but no specific course, filter by courses in that department
      if (selectedDeptId && !selectedCourseId) {
        // Use courses from state if available, otherwise fetch from DB
        let courseIds: (number | string)[] = []
        if (courses.length > 0) {
          courseIds = courses.map(c => c.id)
        } else {
          // Fetch courses directly from DB if not in state yet
          const { data: freshCourses } = await supabase
            .from('courses')
            .select('id')
            .eq('department_id', selectedDeptId)
          courseIds = (freshCourses || []).map(c => c.id)
        }
        
        if (courseIds.length > 0) {
          query = query.in('course_id', courseIds)
        } else {
          // No courses in this department, return empty
          setAttendances([])
          setStats({ totalRecords: 0, uniqueStudents: 0, uniqueCourses: 0, attendanceRate: 0 })
          setLoading(false)
          return
        }
      }

      const { data, error } = await query.limit(1000) // Limit to 1000 records for performance

      console.log('Attendances query result:', { dataCount: data?.length || 0, error })

      if (error) {
        console.error('Error loading attendances:', error)
        alert('Yoklama verileri yüklenirken hata: ' + error.message)
        setAttendances([])
        setStats({ totalRecords: 0, uniqueStudents: 0, uniqueCourses: 0, attendanceRate: 0 })
        setLoading(false)
        return
      }

      // Fetch profiles and courses separately (more reliable than joins)
      const attendanceData = data || []
      
      if (attendanceData.length === 0) {
        setAttendances([])
        setStats({ totalRecords: 0, uniqueStudents: 0, uniqueCourses: 0, attendanceRate: 0 })
        setLoading(false)
        return
      }

      const studentIds = [...new Set(attendanceData.map(a => a.student_id))].filter(Boolean)
      const courseIds = [...new Set(attendanceData.map(a => a.course_id))].filter(Boolean)

      const [profilesResult, coursesResult] = await Promise.all([
        studentIds.length > 0
          ? supabase.from('profiles').select('id, full_name, email').in('id', studentIds)
          : Promise.resolve({ data: [], error: null }),
        courseIds.length > 0
          ? supabase.from('courses').select('id, name, code').in('id', courseIds)
          : Promise.resolve({ data: [], error: null }),
      ])

      if (profilesResult.error) {
        console.warn('Error fetching profiles:', profilesResult.error)
      }
      if (coursesResult.error) {
        console.warn('Error fetching courses:', coursesResult.error)
      }

      const profilesMap = new Map((profilesResult.data || []).map(p => [p.id, p]))
      const coursesMap = new Map((coursesResult.data || []).map(c => [c.id, c]))

      let enrichedData = attendanceData.map(att => ({
        ...att,
        profiles: profilesMap.get(att.student_id) || null,
        courses: coursesMap.get(att.course_id) || null,
      }))

      // Apply class filter if selected
      if (selectedClassId) {
        const { data: studentsData } = await supabase
          .from('students')
          .select('id, email, class_id')
          .eq('class_id', selectedClassId)

        if (studentsData && studentsData.length > 0) {
          const studentEmails = studentsData.map(s => s.email).filter(Boolean)
          if (studentEmails.length > 0) {
            const { data: profilesData } = await supabase
              .from('profiles')
              .select('id')
              .in('email', studentEmails)

            if (profilesData && profilesData.length > 0) {
              const profileIds = profilesData.map(p => p.id)
              enrichedData = enrichedData.filter(a => profileIds.includes(a.student_id))
            } else {
              enrichedData = []
            }
          } else {
            enrichedData = []
          }
        } else {
          enrichedData = []
        }
      }

      let filteredData = enrichedData

      setAttendances(filteredData)

      // Calculate stats
      const uniqueStudents = new Set(filteredData.map(a => a.student_id)).size
      const uniqueCourses = new Set(filteredData.map(a => a.course_id)).size
      setStats({
        totalRecords: filteredData.length,
        uniqueStudents,
        uniqueCourses,
        attendanceRate: 0 // Would need total expected vs actual to calculate
      })
    } catch (error: any) {
      console.error('Error:', error)
      alert('Hata: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  const selectedDept = useMemo(() => departments.find(d => d.id === selectedDeptId), [departments, selectedDeptId])

  function formatDate(dateString: string) {
    const date = new Date(dateString)
    return date.toLocaleDateString('tr-TR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  function exportToCSV() {
    if (attendances.length === 0) {
      alert('Dışa aktarılacak veri yok')
      return
    }

    const headers = ['Tarih', 'Öğrenci Adı', 'Öğrenci Email', 'Ders', 'Hafta', 'Yöntem']
    const rows = attendances.map(a => [
      formatDate(a.marked_at),
      a.profiles?.full_name || 'Bilinmiyor',
      a.profiles?.email || 'Bilinmiyor',
      a.courses?.name || `Ders #${a.course_id}`,
      a.week_number.toString(),
      a.method || 'qr'
    ])

    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
    ].join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `yoklama-raporu-${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }

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
                <h1 className="text-2xl font-bold text-academic-primary">Yoklama Raporları</h1>
                <p className="text-academic-text-secondary">Detaylı yoklama kayıtları ve istatistikler</p>
              </div>
            </div>
            {attendances.length > 0 && (
              <button onClick={exportToCSV} className="btn-primary flex items-center space-x-2">
                <Download className="w-4 h-4" />
                <span>CSV İndir</span>
              </button>
            )}
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Filters */}
        <div className="card mb-6">
          <div className="flex items-center space-x-2 mb-4">
            <Filter className="w-5 h-5 text-academic-primary" />
            <h2 className="text-lg font-semibold text-academic-text-primary">Filtreler</h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-1">Bölüm</label>
              <select
                value={selectedDeptId}
                onChange={e => {
                  setSelectedDeptId(e.target.value)
                  setSelectedCourseId('')
                  setSelectedClassId('')
                }}
                className="input-field"
              >
                <option value="">Tüm Bölümler</option>
                {departments.map(d => (
                  <option key={d.id} value={d.id}>
                    {d.name} ({d.code})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-1">Ders</label>
              <select
                value={selectedCourseId}
                onChange={e => setSelectedCourseId(e.target.value)}
                className="input-field"
                disabled={!selectedDeptId}
              >
                <option value="">Tüm Dersler</option>
                {courses.map(c => (
                  <option key={c.id} value={c.id}>
                    {c.name} {c.code ? `(${c.code})` : ''}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-1">Sınıf</label>
              <select
                value={selectedClassId}
                onChange={e => setSelectedClassId(e.target.value)}
                className="input-field"
                disabled={!selectedDeptId}
              >
                <option value="">Tüm Sınıflar</option>
                {classes.map(cls => (
                  <option key={cls.id} value={cls.id}>
                    {cls.name} ({cls.academic_year})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-1">Başlangıç Tarihi</label>
              <input
                type="date"
                value={dateFrom}
                onChange={e => setDateFrom(e.target.value)}
                className="input-field"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-academic-text-primary mb-1">Bitiş Tarihi</label>
              <input
                type="date"
                value={dateTo}
                onChange={e => setDateTo(e.target.value)}
                className="input-field"
              />
            </div>
          </div>
        </div>

        {/* Statistics */}
        {attendances.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div className="card">
              <div className="flex items-center space-x-3">
                <div className="p-3 bg-academic-primary-light rounded-lg">
                  <CheckCircle className="w-6 h-6 text-academic-primary" />
                </div>
                <div>
                  <p className="text-sm text-academic-text-secondary">Toplam Kayıt</p>
                  <p className="text-2xl font-bold text-academic-text-primary">{stats.totalRecords}</p>
                </div>
              </div>
            </div>

            <div className="card">
              <div className="flex items-center space-x-3">
                <div className="p-3 bg-academic-secondary-light rounded-lg">
                  <Users className="w-6 h-6 text-academic-secondary" />
                </div>
                <div>
                  <p className="text-sm text-academic-text-secondary">Benzersiz Öğrenci</p>
                  <p className="text-2xl font-bold text-academic-text-primary">{stats.uniqueStudents}</p>
                </div>
              </div>
            </div>

            <div className="card">
              <div className="flex items-center space-x-3">
                <div className="p-3 bg-academic-success-light rounded-lg">
                  <TrendingUp className="w-6 h-6 text-academic-success" />
                </div>
                <div>
                  <p className="text-sm text-academic-text-secondary">Benzersiz Ders</p>
                  <p className="text-2xl font-bold text-academic-text-primary">{stats.uniqueCourses}</p>
                </div>
              </div>
            </div>

            <div className="card">
              <div className="flex items-center space-x-3">
                <div className="p-3 bg-academic-error-light rounded-lg">
                  <Calendar className="w-6 h-6 text-academic-error" />
                </div>
                <div>
                  <p className="text-sm text-academic-text-secondary">Ortalama/Gün</p>
                  <p className="text-2xl font-bold text-academic-text-primary">
                    {dateFrom && dateTo
                      ? Math.round(
                          stats.totalRecords /
                            Math.max(1, Math.ceil((new Date(dateTo).getTime() - new Date(dateFrom).getTime()) / (1000 * 60 * 60 * 24)))
                        )
                      : '-'}
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Attendance List */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-academic-text-primary">Yoklama Kayıtları</h2>
            {loading && <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-academic-primary"></div>}
          </div>

          {attendances.length === 0 && !loading ? (
            <div className="text-center py-12">
              <Calendar className="w-16 h-16 text-academic-text-secondary mx-auto mb-4" />
              <p className="text-academic-text-secondary">
                {selectedDeptId || selectedCourseId || dateFrom || dateTo
                  ? 'Seçilen filtreler için yoklama kaydı bulunamadı.'
                  : 'Yoklama kaydı görüntülemek için filtre seçin.'}
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-academic-divider">
                    <th className="text-left py-3 px-4 text-sm font-medium text-academic-text-primary">Tarih</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-academic-text-primary">Öğrenci</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-academic-text-primary">Email</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-academic-text-primary">Ders</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-academic-text-primary">Hafta</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-academic-text-primary">Yöntem</th>
                  </tr>
                </thead>
                <tbody>
                  {attendances.map(att => (
                    <tr key={att.id} className="border-b border-academic-divider hover:bg-academic-surface">
                      <td className="py-3 px-4 text-sm text-academic-text-primary">{formatDate(att.marked_at)}</td>
                      <td className="py-3 px-4 text-sm text-academic-text-primary">
                        {att.profiles?.full_name || 'Bilinmiyor'}
                      </td>
                      <td className="py-3 px-4 text-sm text-academic-text-secondary">
                        {att.profiles?.email || 'Bilinmiyor'}
                      </td>
                      <td className="py-3 px-4 text-sm text-academic-text-primary">
                        {att.courses?.name || `Ders #${att.course_id}`}
                        {att.courses?.code && <span className="text-academic-text-secondary ml-1">({att.courses.code})</span>}
                      </td>
                      <td className="py-3 px-4 text-sm text-academic-text-primary">{att.week_number}</td>
                      <td className="py-3 px-4 text-sm text-academic-text-secondary">
                        <span className="px-2 py-1 rounded-full bg-academic-chip text-academic-text-primary">
                          {att.method || 'qr'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  )
}

