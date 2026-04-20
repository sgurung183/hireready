from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import ListFlowable, ListItem, Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle


ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT / "output" / "pdf"
TMP_DIR = ROOT / "tmp" / "pdfs"
OUTPUT_PATH = OUTPUT_DIR / "hireready-app-summary.pdf"


def bullet_list(items, style, left_indent=12):
    return ListFlowable(
        [ListItem(Paragraph(item, style)) for item in items],
        bulletType="bullet",
        start="circle",
        leftIndent=left_indent,
        bulletFontName="Helvetica",
        bulletFontSize=7,
        bulletOffsetY=1,
    )


def build_pdf():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    TMP_DIR.mkdir(parents=True, exist_ok=True)

    styles = getSampleStyleSheet()
    styles.add(
        ParagraphStyle(
            name="TitleSmall",
            parent=styles["Title"],
            fontName="Helvetica-Bold",
            fontSize=17,
            leading=19,
            textColor=colors.HexColor("#17324D"),
            spaceAfter=8,
        )
    )
    styles.add(
        ParagraphStyle(
            name="SectionHead",
            parent=styles["Heading2"],
            fontName="Helvetica-Bold",
            fontSize=10,
            leading=11,
            textColor=colors.HexColor("#0F4C5C"),
            spaceAfter=3,
            spaceBefore=4,
        )
    )
    styles.add(
        ParagraphStyle(
            name="BodyTight",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=8.2,
            leading=10,
            textColor=colors.HexColor("#1E293B"),
            spaceAfter=2,
        )
    )
    styles.add(
        ParagraphStyle(
            name="BulletTight",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=7.9,
            leading=9.1,
            textColor=colors.HexColor("#1E293B"),
            leftIndent=0,
            spaceAfter=0,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Meta",
            parent=styles["BodyText"],
            fontName="Helvetica-Oblique",
            fontSize=6.8,
            leading=8,
            textColor=colors.HexColor("#475569"),
            spaceAfter=4,
        )
    )

    left_col = [
        Paragraph("HireReady App Summary", styles["TitleSmall"]),
        Paragraph(
            "Repo-based one-page snapshot. Statements below are limited to evidence found in the codebase on March 23, 2026.",
            styles["Meta"],
        ),
        Paragraph("What It Is", styles["SectionHead"]),
        Paragraph(
            "HireReady is currently a Spring Boot backend starter project. The repo implements application bootstrapping plus a persisted <b>User</b> model with email, password, role, visa status, and creation timestamp fields; broader product behavior is <b>Not found in repo</b>.",
            styles["BodyTight"],
        ),
        Paragraph("Who It's For", styles["SectionHead"]),
        Paragraph(
            "Primary persona appears to be teams building an authentication- or user-management-oriented backend for job-seeker workflows. End-user persona details are <b>Not found in repo</b>.",
            styles["BodyTight"],
        ),
        Paragraph("What It Does", styles["SectionHead"]),
        bullet_list(
            [
                "Bootstraps a Spring Boot application from a single `main` entrypoint.",
                "Defines a JPA `users` table model with required `fullName`, `email`, and `password` fields.",
                "Stores `role` as `USER` or `ADMIN` enum values.",
                "Stores `visaStatus` as `F1`, `OPT`, `CPT`, `H1B`, `CITIZEN`, or `OTHER`.",
                "Auto-populates `createdAt` on insert via `@PrePersist`.",
                "Provides repository methods to look up users by email and check email existence.",
                "Includes declared dependencies for web, validation, security, PostgreSQL, and JWT support; actual API/security implementation is <b>Not found in repo</b>.",
            ],
            styles["BulletTight"],
        ),
        Spacer(1, 0.08 * inch),
        Paragraph("How To Run", styles["SectionHead"]),
        bullet_list(
            [
                "Install Java 21.",
                "From the repo root, run `./mvnw spring-boot:run` on macOS/Linux or `mvnw.cmd spring-boot:run` on Windows.",
                "If startup fails, add the missing runtime configuration first: datasource, PostgreSQL connection values, and any JWT/security properties are <b>Not found in repo</b>.",
            ],
            styles["BulletTight"],
        ),
    ]

    arch_bullets = bullet_list(
        [
            "<b>Application:</b> `HirereadyApplication` starts Spring Boot auto-configuration.",
            "<b>Persistence:</b> `User` is a JPA entity mapped to `users`; `UserRepository` extends `JpaRepository<User, Long>`.",
            "<b>Domain enums:</b> `Role` and `VisaStatus` constrain stored values.",
            "<b>Config:</b> `application.properties` only sets `spring.application.name=hireready`.",
            "<b>Declared services/deps:</b> Spring Web, Security, Validation, JPA, PostgreSQL driver, DevTools, and JJWT libs are in `pom.xml`.",
            "<b>Data flow seen in repo:</b> App start -> Spring Boot context -> JPA entity/repository layer -> database interactions implied by JPA mapping.",
            "<b>Not found in repo:</b> controllers, service classes, security configuration, JWT usage code, request/response DTOs, migration scripts, frontend, deployment setup.",
        ],
        styles["BulletTight"],
        left_indent=10,
    )

    right_col = [
        Paragraph("How It Works", styles["SectionHead"]),
        arch_bullets,
        Spacer(1, 0.1 * inch),
        Paragraph("Evidence Used", styles["SectionHead"]),
        bullet_list(
            [
                "`pom.xml`",
                "`src/main/java/com/hireready/hireready/HirereadyApplication.java`",
                "`src/main/java/com/hireready/hireready/entity/User.java`",
                "`src/main/java/com/hireready/hireready/entity/Role.java`",
                "`src/main/java/com/hireready/hireready/entity/VisaStatus.java`",
                "`src/main/java/com/hireready/hireready/repository/UserRepository.java`",
                "`src/main/resources/application.properties`",
            ],
            styles["BulletTight"],
            left_indent=10,
        ),
        Spacer(1, 0.08 * inch),
        Paragraph("Notes", styles["SectionHead"]),
        Paragraph(
            "One repository file currently contains a likely typo (`O` before `findByEmail` in `UserRepository`). This summary treats the intended repository method as the evidence-backed feature and does not assume additional behavior beyond the code present.",
            styles["BodyTight"],
        ),
    ]

    table = Table([[left_col, right_col]], colWidths=[4.1 * inch, 2.7 * inch], hAlign="LEFT")
    table.setStyle(
        TableStyle(
            [
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 0),
                ("RIGHTPADDING", (0, 0), (-1, -1), 10),
                ("TOPPADDING", (0, 0), (-1, -1), 0),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 0),
                ("LINEAFTER", (0, 0), (0, 0), 0.5, colors.HexColor("#CBD5E1")),
            ]
        )
    )

    doc = SimpleDocTemplate(
        str(OUTPUT_PATH),
        pagesize=letter,
        leftMargin=0.45 * inch,
        rightMargin=0.45 * inch,
        topMargin=0.4 * inch,
        bottomMargin=0.38 * inch,
        title="HireReady App Summary",
        author="Codex",
    )

    doc.build([table])
    return OUTPUT_PATH


if __name__ == "__main__":
    path = build_pdf()
    print(path)
