-- Fix duplicate course_class_assignments records
-- Keep only one record per (course_id, class_id, academic_year, semester) combination

-- 1. Check duplicates
SELECT 
    course_id,
    class_id,
    academic_year,
    semester,
    COUNT(*) AS duplicate_count
FROM public.course_class_assignments
GROUP BY course_id, class_id, academic_year, semester
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

-- 2. Delete duplicates, keeping only the first one (using created_at or id comparison)
-- For UUID, we'll use a subquery with DISTINCT ON
DELETE FROM public.course_class_assignments
WHERE id NOT IN (
    SELECT DISTINCT ON (course_id, class_id, academic_year, semester) id
    FROM public.course_class_assignments
    ORDER BY course_id, class_id, academic_year, semester, created_at ASC
);

-- 3. Verify - should show no duplicates
SELECT 
    course_id,
    class_id,
    academic_year,
    semester,
    COUNT(*) AS count
FROM public.course_class_assignments
GROUP BY course_id, class_id, academic_year, semester
HAVING COUNT(*) > 1;

-- 4. Show final count
SELECT COUNT(*) AS total_assignments FROM public.course_class_assignments;

