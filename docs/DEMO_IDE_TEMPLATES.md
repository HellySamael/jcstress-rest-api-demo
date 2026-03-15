# Demo IDE Templates

## Demo Branch Map

- `10` -> `demo/10-hashmap-racy`
- `20` -> `demo/20-sync`
- `30` -> `demo/30-concurrent-hashmap`
- `40` -> `demo/40-jmm-threadsafe`
- `50` -> `demo/50-db-racy`
- `60` -> `demo/60-db-safe`

## VSCode Template

### Switch branch from Command Palette

1. Open `Terminal -> Run Task...`
2. Run one of:
- `Demo: Step 10 (hashmap)`
- `Demo: Step 20 (sync)`
- `Demo: Step 30 (concurrent)`
- `Demo: Step 40 (threadsafe)`
- `Demo: Step 50 (racydb)`
- `Demo: Step 60 (safedb)`

### Run app

1. Open `Run and Debug`
2. Use one of:
- `Demo API (branch default)`
- `Demo API (jmm)`
- `Demo API (distributed)`

### Run stress tests

- `Demo: JCStress JMM`
- `Demo: JCStress DB`

## IntelliJ Template

### External Tool template: switch branch

Create one External Tool per step (duplicate and change `-Step` value).

- Name: `Demo Step 10` (then `20`, `30`, `40`, `50`, `60`)
- Program: `powershell.exe`
- Arguments:
  `-ExecutionPolicy Bypass -File $ProjectFileDir$/scripts/demo/switch-demo-branch.ps1 -Step 10`
- Working directory:
  `$ProjectFileDir$`

Do the same with `-Step 20`, `-Step 30`, `-Step 40`, `-Step 50`, `-Step 60`.

### IntelliJ Run configuration template

- Main class: `com.example.demo.DemoApplication`
- Working directory: `$ProjectFileDir$`
- Program arguments (optional):
- `jmm`
- `distributed`
- `hashmap`
- `sync`
- `concurrent`
- `threadsafe`
- `racydb`
- `safedb`

## CLI Template

- PowerShell: `./scripts/demo/switch-demo-branch.ps1 -Step 30`
- Bash: `./scripts/demo/switch-demo-branch.sh 30`

Add `-Force` (PowerShell) or `--force` (Bash) if you intentionally switch with local changes.
