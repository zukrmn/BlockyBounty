# BlockyBounty

Plugin de recompensas (bounties) para o servidor BlockyCRAFT com suporte ao BlockyGroups. Permite que jogadores coloquem recompensas em ferros pela cabeça de outros jogadores.

## Features

- Comando `/bounty <jogador> <quantidade>`: Cria uma recompensa sobre outro jogador, cobrando ferros do inventário.
- Comando `/bounty remover <jogador>`: Remove sua própria recompensa e devolve os ferros.
- Comando `/bounty list`: Lista todas as recompensas ativas do servidor.
- Paga a recompensa automaticamente ao jogador que matar o alvo (desde que não estejam na mesma grupo).
- Suporte completo a grupos pelo plugin BlockyGroups (impede bounty entre membros da mesma grupo).
- Mensagens coloridas e personalizáveis via arquivo `messages.properties`.
- Banco de dados próprio com SQLite (`bounties.db`) para persistência das recompensas.

## Comandos

- `/bounty <jogador> <quantidade>`  
  Coloca uma recompensa de ferros pela cabeça do jogador escolhido.  
- `/bounty remover <jogador>`  
  Remove sua própria recompensa sobre o jogador alvo.  
- `/bounty list`  
  Lista todas as recompensas ativas.

## Permissões

- `blockybounty.bounty`  
  Permite que jogadores usem todos os comandos do plugin.

## Integração

- **BlockyGroups (opcional, recomendado):** Detecção automática de grupo para impedir bounty entre membros de mesma grupo.
- 100% compatível com outros plugins Blocky* do seu servidor.

## Banco de Dados

O arquivo de banco de dados `bounties.db` será gerado automaticamente na pasta do plugin.

## Reportar bugs ou requisitar features
Reporte bugs ou sugira novas funcionalidades na seção [Issues](https://github.com/andradecore/BlockyBounty/issues) do projeto.

## Contato:
- Discord: https://discord.gg/tthPMHrP