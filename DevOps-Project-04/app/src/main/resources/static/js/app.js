// Auto-highlight low/out-of-stock rows
document.querySelectorAll('.status-badge.LOW_STOCK').forEach(el => {
  el.closest('tr').style.background = 'rgba(245,158,11,0.04)';
});
document.querySelectorAll('.status-badge.OUT_OF_STOCK').forEach(el => {
  el.closest('tr').style.background = 'rgba(239,68,68,0.04)';
});

console.log('Inventory Manager - DevOps Project 04 loaded ✅');
