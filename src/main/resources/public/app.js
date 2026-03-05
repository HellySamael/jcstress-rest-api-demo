// Simple vanilla JS app for pizza voting
// It talks to the backend API endpoints:
// POST /votes/{itemId} -> increments and returns {votes: newCount}
// GET  /votes -> returns { itemId: count, ... }

const API_BASE = '/'; // same origin

const PIZZAS = [
  { id: 'margherita', name: 'Margherita', ingredients: ['Tomate', 'Mozzarella', 'Basilic'], image: 'images/margherita.svg' },
  { id: 'pepperoni', name: 'Pepperoni', ingredients: ['Tomate', 'Mozzarella', 'Pepperoni'], image: 'images/pepperoni.svg' },
  { id: 'funghi', name: 'Funghi', ingredients: ['Tomate', 'Mozzarella', 'Champignons'], image: 'images/funghi.svg' },
  { id: 'quattro', name: 'Quattro Formaggi', ingredients: ['Mozzarella', 'Gorgonzola', 'Parmesan'], image: 'images/quattro.svg' }
];

function q(sel) { return document.querySelector(sel); }

// (lastVoter removed: name field is not used in the demo)

async function fetchVotes() {
  const res = await fetch(API_BASE + 'votes');
  if (!res.ok) throw new Error('Erreur réseau: ' + res.status);
  return await res.json();
}

async function vote(itemId) {
  let url = API_BASE + 'votes/' + encodeURIComponent(itemId);
  const res = await fetch(url, { method: 'POST' });
  if (!res.ok) throw new Error('Erreur vote: ' + res.status);
  return await res.json();
}

function showToast(msg, timeout = 2200) {
  const t = q('#toast');
  t.textContent = msg;
  t.classList.add('visible');
  clearTimeout(t._to);
  t._to = setTimeout(() => t.classList.remove('visible'), timeout);
}

function render(votesMap) {
  const container = q('#items');
  container.innerHTML = '';
  PIZZAS.forEach(p => {
    const count = votesMap[p.id] || 0;
    const div = document.createElement('article');
    div.className = 'item';
    div.innerHTML = `
      <div class="media">
        <img src="${p.image}" alt="${p.name}" />
        <div class="meta">
          <h2>${p.name}</h2>
          <div class="ingredients">${p.ingredients.join(', ')}</div>
        </div>
      </div>
      <div class="count-row">
        <div class="count" data-id="${p.id}">${count}</div>
        <div>
          <button class="vote-btn" data-id="${p.id}">Voter</button>
        </div>
      </div>
    `;
    container.appendChild(div);
  });

  container.querySelectorAll('.vote-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.getAttribute('data-id');
      btn.disabled = true;
      try {
        await vote(id);
        showToast(`Merci pour votre vote`);
        // after a vote, refresh the whole view to update counts and total
        await refresh();
      } catch (err) {
        console.error(err);
        alert('Erreur lors du vote. Vérifiez que le backend est lancé sur le même hôte et port.');
      } finally {
        btn.disabled = false;
      }
    });
  });

  // Update total display
  updateTotal(votesMap);
}

// Update the total votes display (sum of all counts in votesMap)
function updateTotal(votesMap) {
  try {
    const totalEl = q('#totalVotes');
    if (!totalEl) return;
    const values = Object.values(votesMap || {});
    const total = values.reduce((acc, v) => acc + (Number.isFinite(Number(v)) ? Number(v) : 0), 0);
    totalEl.textContent = total;
  } catch (err) {
    console.warn('Could not update total votes:', err);
  }
}

async function refresh() {
  try {
    const votes = await fetchVotes();
    render(votes);
  } catch (err) {
    console.error(err);
    q('#items').innerHTML = '<p>Impossible de récupérer les votes. Assurez-vous que le backend est en cours d\'exécution.</p>';
  }
}

// Reset (admin) - call DELETE /votes
async function reset() {
  if (!confirm('Réinitialiser tous les votes ?')) return;
  const res = await fetch(API_BASE + 'votes', { method: 'DELETE' });
  if (!res.ok) { alert('Échec du reset: ' + res.status); return; }
  await refresh();
}

function escapeHtml(unsafe) {
  return unsafe.replace(/[&<>\"']/g, function(m) { return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#039;"})[m]; });
}

// Wire UI
q('#refresh').addEventListener('click', async () => {
  // manual refresh should use the same safe path and reset the timer
  await safeRefresh();
  restartAutoRefresh();
});
q('#reset').addEventListener('click', reset);

// Auto-refresh machinery
const AUTO_REFRESH_INTERVAL_MS = 5000; // default 5s, adjustable
let autoRefreshTimer = null;
let isRefreshing = false;

// Prevent overlapping refreshes
async function safeRefresh() {
  if (isRefreshing) return;
  isRefreshing = true;
  try {
    await refresh();
  } catch (err) {
    // refresh() already logs errors; we can optionally surface them
  } finally {
    isRefreshing = false;
  }
}

function startAutoRefresh(intervalMs = AUTO_REFRESH_INTERVAL_MS) {
  stopAutoRefresh();
  autoRefreshTimer = setInterval(() => {
    // Do not refresh when page is hidden (save resources)
    if (document.hidden) return;
    safeRefresh();
  }, intervalMs);
}

function stopAutoRefresh() {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer);
    autoRefreshTimer = null;
  }
}

function restartAutoRefresh() {
  stopAutoRefresh();
  startAutoRefresh();
}

// Pause/resume based on page visibility to be "propre"
document.addEventListener('visibilitychange', () => {
  if (document.hidden) {
    // pause polling
    stopAutoRefresh();
  } else {
    // resume polling when user comes back
    startAutoRefresh();
  }
});

// Initial load via safeRefresh + start polling
safeRefresh();
startAutoRefresh();

// Fetch server implementation info and display it
(async function showImpl() {
  try {
    const res = await fetch(API_BASE + 'info');
    if (!res.ok) return;
    const json = await res.json();
    const impl = json.impl || 'unknown';
    const el = q('#implName');
    if (el) el.textContent = impl;
  } catch (err) {
    console.warn('Could not fetch /info:', err);
  }
})();
