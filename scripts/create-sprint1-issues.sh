#!/usr/bin/env bash
# Creates the Sprint 1 Product Backlog (17 issues) on GitHub.
# Prerequisites: gh auth login; labels + "Sprint 1" milestone created (./scripts/setup-github.sh).
#
# Usage: ./scripts/create-sprint1-issues.sh <owner>/<repo> [project-title]
#   project-title (optional): GitHub Project to add issues to, e.g. "StudyMatch"
#   (requires: gh auth refresh -s project)
set -euo pipefail

REPO="${1:?Usage: $0 <owner>/<repo> [project-title]}"
PROJECT="${2:-}"
MILESTONE="Sprint 1"

# create <title> <labels comma-separated> <body>  -> echoes issue number
create() {
  local title="$1" labels="$2" body="$3" args=()
  args=(--repo "$REPO" --title "$title" --body "$body" --milestone "$MILESTONE")
  IFS=',' read -ra L <<< "$labels"
  for l in "${L[@]}"; do args+=(--label "$l"); done
  [[ -n "$PROJECT" ]] && args+=(--project "$PROJECT")
  local url; url=$(gh issue create "${args[@]}")
  echo "  #${url##*/}  $title" >&2
  echo "${url##*/}"
}

echo "==> Creating Sprint 1 backlog in $REPO"

# ---------- Organisation & workflow ----------
I1=$(create "Set up GitHub repository and project board" "type: chore,area: devops,priority: high" "$(cat <<EOF
**Suggested owner:** Daniel

Configure the repository as the team's primary project-management environment.

### Done when
- [ ] README.md and .gitignore committed
- [ ] Labels, "Sprint 1" milestone and Project board (Backlog, To Do, In Progress, In Review, Done) created
- [ ] main protected (PR required, 1 approval, no force-push); squash merge only; auto-delete branches on
- [ ] All 5 members invited and accepted
EOF
)")

I2=$(create "Document team workflow (branching, PRs, reviews)" "type: docs,area: quality,priority: high" "$(cat <<EOF
**Suggested owner:** Daniel · **Reviewers:** whole team

Review and approve CONTRIBUTING.md: branch naming, issue assignment, PR rules, review rotation and branch deletion after merge.

### Done when
- [ ] Every member has read and approved the PR (at least one comment each)
- [ ] Agreed changes applied and merged
EOF
)")

I3=$(create "Agree coding conventions and Sprint 1 Definition of Done" "type: docs,area: quality,priority: high" "$(cat <<EOF
**Suggested owner:** whole team

Review docs/CODING_CONVENTIONS.md and docs/DEFINITION_OF_DONE.md and finalise the language-specific sections once the stack is decided (ADR-0001).

### Done when
- [ ] Package/folder structure agreed for backend and frontend
- [ ] Formatter/linter config committed
- [ ] DoD approved by all members
EOF
)")

I4=$(create "Each member adds their role to the README (first PR)" "type: docs,area: quality,priority: medium" "$(cat <<EOF
**Suggested owner:** every member (one PR each)

Practise the full Issue -> Branch -> PR -> Review -> Merge flow early. Each member opens a branch docs/<issue-number>-role-<name>, fills in their Sprint 1 role in the README team table and opens a PR reviewed by the next person in the rotation.

### Done when
- [ ] 5 PRs merged (one per member), each approved by another member
- [ ] Branches deleted after merge
EOF
)")

# ---------- Technical decisions ----------
I5=$(create "ADR-0001: decide and justify technology stack" "type: docs,area: architecture,priority: high" "$(cat <<EOF
**Suggested owner:** Daniel + whole team

Vote on docs/decisions/ADR-0001-technology-stack.md (proposal: React + Vite + TS, Java 21 + Spring Boot 3, MySQL 8, Maven).

### Done when
- [ ] Decision recorded with justification (suitability, maintainability, testability, integration, team knowledge, tooling)
- [ ] Status changed to Accepted
- [ ] README prerequisites updated

**Blocks:** backend, persistence and frontend skeleton issues.
EOF
)")

I6=$(create "ADR-0002: database strategy (local Docker, cloud demo, migrations)" "type: docs,area: database,priority: medium" "$(cat <<EOF
**Suggested owner:** Guilherme · **Depends on:** #${I5}

Document how persistence is handled per environment: MySQL in Docker for development and tests, managed MySQL (e.g. Aiven free tier) for the shared demo, schema versioned with Flyway, credentials via environment variables.

### Done when
- [ ] ADR-0002 merged in docs/decisions
- [ ] .env.example committed (no real secrets)
EOF
)")

# ---------- Analysis & modelling ----------
I7=$(create "Brainstorm use cases and domain concepts on FigJam board" "type: docs,area: domain,priority: high" "$(cat <<EOF
**Suggested owner:** whole team

Every member adds ideas (use cases, concepts, questions, new features) to the 6 capability areas on the team FigJam board, then the team votes.

### Done when
- [ ] Each member contributed to every area
- [ ] Voting done; agreed items moved to "Decisions"
- [ ] Open questions listed for the use-case and domain issues
EOF
)")

UC_BODY_FOOTER="Use docs/use-cases/UC-00-template.md for each use case: name and objective, primary actor(s), main success scenario, alternative/exceptional flows, business rules, domain concepts revealed. Update the index in docs/use-cases/README.md."

I8=$(create "Use cases: student identity and academic trajectory" "type: docs,area: domain,priority: high" "$(cat <<EOF
**Suggested owner:** Hugo · **Depends on:** #${I7}

Capability areas: Student and academic identity; Academic trajectory (course units, enrolments, attempts, grades, progression).

${UC_BODY_FOOTER}

### Done when
- [ ] At least 2 use cases per area documented
- [ ] Actors and permissions identified
EOF
)")

I9=$(create "Use cases: competencies, student profile and cold start" "type: docs,area: domain,priority: high" "$(cat <<EOF
**Suggested owner:** Hugo · **Depends on:** #${I7}

Capability areas: Competencies (definition, link to course units, scale, contribution rules); Student profile (content and presentation); Cold start (strategy for students with little or no history).

${UC_BODY_FOOTER}

### Done when
- [ ] At least 1–2 use cases per area documented
- [ ] Cold-start strategy described and justified
EOF
)")

I10=$(create "Use cases: grouping context" "type: docs,area: domain,priority: high" "$(cat <<EOF
**Suggested owner:** Hugo · **Depends on:** #${I7}

Capability area: information that defines the context in which groups will be formed (course unit, activity, target group size, constraints).

${UC_BODY_FOOTER}

### Done when
- [ ] Use cases for creating a context and defining constraints documented
- [ ] Business rules (e.g. group size limits) listed
EOF
)")

I11=$(create "Propose and justify at least 2 additional value-adding use cases" "type: docs,area: domain,proposal,priority: high" "$(cat <<EOF
**Suggested owner:** Hugo + whole team · **Depends on:** #${I7}

Pick the best ideas from the "Our proposals" area (e.g. explain my group, peer review feeding the profile, availability matching, privacy/consent) and document them as full use cases with a justification of why they improve StudyMatch.

### Done when
- [ ] At least 2 use cases documented with template + "Why it adds value" section
- [ ] Chosen by team vote
EOF
)")

I12=$(create "Initial domain model (UML class diagram)" "type: docs,area: domain,priority: high" "$(cat <<EOF
**Suggested owner:** Diego · **Depends on:** #${I8}, #${I9}, #${I10}

Create the initial domain model from the use-case analysis (starting point: domain sketch on the FigJam board). It must represent the domain, not implementation (no controllers, repositories or tables).

### Done when
- [ ] Entities, value concepts, key attributes, relationships and cardinalities
- [ ] Domain constraints noted
- [ ] Concepts for academic history and student profile included
- [ ] Source file (PlantUML/draw.io) + PNG export in docs/domain
- [ ] Names consistent with the use cases
EOF
)")

I13=$(create "Architecture diagram and component explanation" "type: docs,area: architecture,priority: high" "$(cat <<EOF
**Suggested owner:** Daniel · **Depends on:** #${I5}

Document the initial architecture in docs/architecture.

### Done when
- [ ] Diagram of main components/modules and their dependencies
- [ ] Responsibilities of each component
- [ ] Frontend <-> backend communication (REST/JSON)
- [ ] Persistence approach
- [ ] Where domain/business rules live
EOF
)")

# ---------- Full-stack skeleton ----------
I14=$(create "Backend skeleton with health endpoint" "type: feature,area: backend,priority: high" "$(cat <<EOF
**Suggested owner:** Guilherme · **Depends on:** #${I5}

Create the backend project in backend/ following the agreed package structure.

### Done when
- [ ] Project builds with the chosen build tool (e.g. mvn verify)
- [ ] App starts locally
- [ ] GET /api/health returns 200 with a JSON status
- [ ] At least one unit/integration test (JUnit 5)
- [ ] Run instructions in README
EOF
)")

I15=$(create "Configure persistence (MySQL + migrations) with sample students" "type: feature,area: database,area: backend,priority: high" "$(cat <<EOF
**Suggested owner:** Guilherme · **Depends on:** #${I6}, #${I14}

### Done when
- [ ] docker-compose.yml starts MySQL locally
- [ ] First migration creates a student table and inserts sample data
- [ ] GET /api/students reads from the database
- [ ] Credentials read from environment variables
- [ ] Integration test against a real database (e.g. Testcontainers)
EOF
)")

I16=$(create "Frontend skeleton showing students from the API" "type: feature,area: frontend,priority: high" "$(cat <<EOF
**Suggested owner:** Erzhan · **Depends on:** #${I5}, #${I14}

Create the frontend project in frontend/ with an API client layer.

### Done when
- [ ] App starts locally (e.g. npm run dev) and builds (npm run build)
- [ ] Page shows backend health status and the list of students from GET /api/students
- [ ] Loading and error states handled
- [ ] Dev proxy/CORS configured
- [ ] End-to-end interaction demonstrated (frontend -> API -> DB)
EOF
)")

# ---------- Wrap-up ----------
I17=$(create "Prepare Sprint 1 Review" "type: docs,priority: medium" "$(cat <<EOF
**Suggested owner:** whole team

### Done when
- [ ] Demo script: GitHub organisation and workflow, technology choices, use cases + proposals, domain model, architecture, running skeleton
- [ ] One complete example of Issue -> Branch -> PR -> Review -> Merge selected
- [ ] At least one PR with a meaningful review discussion (not just an approval)
- [ ] README setup instructions verified on a clean clone
- [ ] All Sprint 1 issues closed or moved with justification
EOF
)")

echo "==> Done: 17 issues created (#${I1} to #${I17})."
echo "    Next: assign each issue to its owner once they accept the org invite:"
echo "    gh issue edit <n> --repo $REPO --add-assignee <github-username>"
