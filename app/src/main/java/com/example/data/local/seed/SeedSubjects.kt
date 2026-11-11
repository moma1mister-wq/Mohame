package com.example.data.local.seed

import com.example.data.model.SubjectEntity

object SeedSubjects {
    val subjects = listOf(
        // Semester 1
        SubjectEntity(
            id = "computing_fundamental",
            title = "أساسيات الحاسب",
            semester = "sem1",
            icon = "💻",
            colorHex = "#6A1B9A",
            orderIndex = 1
        ),
        SubjectEntity(
            id = "discrete_mathematics",
            title = "الرياضيات المتقطعة",
            semester = "sem1",
            icon = "📐",
            colorHex = "#7B1FA2",
            orderIndex = 2
        ),
        SubjectEntity(
            id = "physics",
            title = "الفيزياء",
            semester = "sem1",
            icon = "⚛️",
            colorHex = "#8E24AA",
            orderIndex = 3
        ),
        SubjectEntity(
            id = "social_skills",
            title = "المهارات الاجتماعية",
            semester = "sem1",
            icon = "🗣️",
            colorHex = "#9C27B0",
            orderIndex = 4
        ),
        SubjectEntity(
            id = "cpp_basics",
            title = "أساسيات البرمجة C++",
            semester = "sem1",
            icon = "🖥️",
            colorHex = "#512DA8",
            orderIndex = 5
        ),
        SubjectEntity(
            id = "fiqh",
            title = "الفقه",
            semester = "sem1",
            icon = "📖",
            colorHex = "#5E35B1",
            orderIndex = 6
        ),

        // Semester 2
        SubjectEntity(
            id = "calculus",
            title = "التفاضل والتكامل",
            semester = "sem2",
            icon = "∫",
            colorHex = "#4A148C",
            orderIndex = 7
        ),
        SubjectEntity(
            id = "internet_technology",
            title = "تكنولوجيا الإنترنت",
            semester = "sem2",
            icon = "🌐",
            colorHex = "#6A1B9A",
            orderIndex = 8
        ),
        SubjectEntity(
            id = "oop_java",
            title = "البرمجة كائنية التوجه OOP",
            semester = "sem2",
            icon = "🧩",
            colorHex = "#7B1FA2",
            orderIndex = 9
        ),
        SubjectEntity(
            id = "operating_systems",
            title = "نظم التشغيل",
            semester = "sem2",
            icon = "⚙️",
            colorHex = "#8E24AA",
            orderIndex = 10
        ),
        SubjectEntity(
            id = "probability_statistics",
            title = "الاحتمالات والإحصاء",
            semester = "sem2",
            icon = "📊",
            colorHex = "#9C27B0",
            orderIndex = 11
        ),
        SubjectEntity(
            id = "entrepreneurship",
            title = "ريادة الأعمال",
            semester = "sem2",
            icon = "💼",
            colorHex = "#512DA8",
            orderIndex = 12
        ),
        SubjectEntity(
            id = "aqeeda_ethics",
            title = "عقيدة وأخلاق",
            semester = "sem2",
            icon = "🕌",
            colorHex = "#5E35B1",
            orderIndex = 13
        ),

        // Exams
        SubjectEntity(
            id = "exams",
            title = "ملفات الامتحانات",
            semester = "tests",
            icon = "📝",
            colorHex = "#311B92",
            orderIndex = 14
        )
    )
}
