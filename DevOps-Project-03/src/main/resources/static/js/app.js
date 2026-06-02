// Auto-dismiss flash messages
document.querySelectorAll('.alert').forEach(el => {
    setTimeout(() => el.remove(), 4000);
});

// Confirm before delete (backup for inline onsubmit)
document.querySelectorAll('.delete-form').forEach(form => {
    form.addEventListener('submit', e => {
        if (!confirm('Delete this task?')) e.preventDefault();
    });
});

console.log('DevOps Task Manager - Project 03 loaded ✅');
