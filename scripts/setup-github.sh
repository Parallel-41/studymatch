#!/usr/bin/env bash
# Configures the GitHub repo: labels, Sprint 1 milestone, merge settings,
# branch protection and Project board. Requires GitHub CLI (`gh auth login`).
#
# Usage: ./scripts/setup-github.sh <owner>/<repo> [sprint1-due-date YYYY-MM-DD]
set -euo pipefail

REPO="${1:?Usage: $0 <owner>/<repo> [YYYY-MM-DD]}"
DUE="${2:-2026-10-07}"
OWNER="${REPO%%/*}"

echo "==> Labels"
label() { gh label create "$1" --repo "$REPO" --color "$2" --description "$3" --force; }
# type
label "type: feature"   "1D76DB" "New functionality / use case"
label "type: bug"       "D73A4A" "Something is not working"
label "type: docs"      "0075CA" "Documentation, models, diagrams"
label "type: test"      "BFD4F2" "Tests only"
label "type: refactor"  "C5DEF5" "Restructuring without behaviour change"
label "type: chore"     "EDEDED" "Build, config, CI, dependencies"
# area
label "area: frontend"     "7057FF" "Web client"
label "area: backend"      "5319E7" "API and business logic"
label "area: database"     "0E8A16" "Persistence, schema, migrations"
label "area: domain"       "006B75" "Domain model / use cases"
label "area: architecture" "0052CC" "Architecture and decisions"
label "area: devops"       "FBCA04" "CI/CD, repo, tooling"
label "area: quality"      "C2E0C6" "Quality practices, conventions, reviews"
# priority
label "priority: high"   "B60205" "Must be done this sprint"
label "priority: medium" "D93F0B" "Should be done this sprint"
label "priority: low"    "FEF2C0" "Nice to have"
# status / other
label "status: blocked"  "000000" "Waiting on something else"
label "proposal"         "F9D0C4" "Team-proposed value-adding use case"
# remove unused defaults
for l in "bug" "documentation" "enhancement" "good first issue" "help wanted" "invalid" "question" "wontfix" "duplicate"; do
  gh label delete "$l" --repo "$REPO" --yes 2>/dev/null || true
done

echo "==> Milestone 'Sprint 1' (due $DUE)"
gh api "repos/$REPO/milestones" -f title="Sprint 1" \
  -f description="Project foundation: workflow, use cases, domain model, architecture, full-stack skeleton" \
  -f due_on="${DUE}T23:59:59Z" >/dev/null || echo "   (milestone may already exist)"

echo "==> Merge settings (squash only, auto-delete branches)"
gh api -X PATCH "repos/$REPO" \
  -F allow_squash_merge=true -F allow_merge_commit=false -F allow_rebase_merge=false \
  -F delete_branch_on_merge=true -f squash_merge_commit_title=PR_TITLE >/dev/null

echo "==> Branch protection on main (needs public repo or paid plan for private repos)"
gh api -X PUT "repos/$REPO/branches/main/protection" --input - >/dev/null <<JSON || echo "   ! Could not protect main — set it manually in Settings > Branches"
{
  "required_status_checks": null,
  "enforce_admins": true,
  "required_pull_request_reviews": { "required_approving_review_count": 1, "dismiss_stale_reviews": true },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false,
  "required_conversation_resolution": true
}
JSON

echo "==> Project board (needs: gh auth refresh -s project)"
gh project create --owner "$OWNER" --title "StudyMatch" >/dev/null \
  && echo "   Created. Link it to the repo and add columns: Backlog, To Do, In Progress, In Review, Done" \
  || echo "   ! Could not create project — create it manually in the org 'Projects' tab"

echo "Done."
