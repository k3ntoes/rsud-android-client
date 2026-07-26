#!/usr/bin/env bash
# Context7 CLI Helper
# Menggunakan ctx7 CLI (tanpa MCP) untuk resolve library ID dan query docs.
#
# Usage:
#   ./scripts/context7.sh resolve <libraryName> [query]
#   ./scripts/context7.sh query <libraryId> <query>
#   ./scripts/context7.sh test

set -euo pipefail

ACTION="${1:-}"
shift 2>/dev/null || true

case "$ACTION" in
  resolve)
    LIBRARY_NAME="${1:-}"
    QUERY="${2:-}"
    if [ -z "$LIBRARY_NAME" ]; then
      echo "Usage: $0 resolve <libraryName> [query]" >&2
      exit 1
    fi
    # ctx7 library command — primary method (tanpa MCP)
    npx -y ctx7 library "$LIBRARY_NAME" "$QUERY" 2>&1
    ;;

  query)
    LIBRARY_ID="${1:-}"
    QUERY="${2:-}"
    if [ -z "$LIBRARY_ID" ] || [ -z "$QUERY" ]; then
      echo "Usage: $0 query <libraryId> <query>" >&2
      exit 1
    fi
    # ctx7 docs command — primary method (tanpa MCP)
    npx -y ctx7 docs "$LIBRARY_ID" "$QUERY" 2>&1
    ;;

  test)
    # Test: resolve library untuk Coil
    echo "=== Testing ctx7 CLI ==="
    npx -y ctx7 library Coil "test" 2>&1 | head -5
    echo ""
    echo "=== ctx7 CLI siap digunakan ==="
    ;;

  *)
    echo "Usage: $0 {resolve|query|test} [args...]" >&2
    echo "" >&2
    echo "Commands:" >&2
    echo "  test                          Test ctx7 CLI connection" >&2
    echo "  resolve <name> [query]        Resolve library to Context7 ID" >&2
    echo "  query <id> <query>            Query documentation" >&2
    echo "" >&2
    echo "Examples:" >&2
    echo "  ./scripts/context7.sh resolve Coil" >&2
    echo "  ./scripts/context7.sh resolve Tink \"Android encryption AEAD\"" >&2
    echo "  ./scripts/context7.sh query /coil-kt/coil \"AsyncImage API\"" >&2
    exit 1
    ;;

esac
