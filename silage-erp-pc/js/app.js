/** Main App Router */
const PAGES = {
  dashboard: Dashboard,
  farmers:   Farmers,
  lands:     Lands,
  harvest:   Harvest,
  inventory: Inventory,
  buyers:    Buyers,
  sellers:   Sellers,
  sales:     Sales,
  costing:   Costing,
  cattle:    CattleModule,
  fattening: Fattening,
  packaging: Packaging,
  reports:   Reports,
};

let currentPage = 'dashboard';

function navigate(page) {
  if (!PAGES[page]) return;
  currentPage = page;

  // Update active nav link
  document.querySelectorAll('#sidebar nav a').forEach(a => {
    a.classList.toggle('active', a.dataset.page === page);
  });

  PAGES[page].render();
}

// Sidebar click events
document.querySelectorAll('#sidebar nav a[data-page]').forEach(a => {
  a.addEventListener('click', e => {
    e.preventDefault();
    navigate(a.dataset.page);
  });
});

// Boot
db.open().then(() => {
  navigate('dashboard');
}).catch(err => {
  document.getElementById('content').innerHTML =
    `<div style="text-align:center;padding:60px;color:red"><h2>Database Error</h2><p>${err.message}</p><p>Try opening this file in Chrome or Edge browser.</p></div>`;
});
