let currentUser = null;

// Initialize Date Picker Default to Tomorrow & Check Active Session Cookie
document.addEventListener('DOMContentLoaded', () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateInput = document.getElementById('appointmentDate');
    if (dateInput) {
        dateInput.value = tomorrow.toISOString().split('T')[0];
        dateInput.min = new Date().toISOString().split('T')[0];
    }
    checkSession();
});

// Auto-restore session from HTTP Cookie
async function checkSession() {
    try {
        const res = await fetch('/api/auth?action=check');
        const data = await res.json();
        if (data.authenticated && data.user) {
            currentUser = data.user;
            document.getElementById('loginSection').style.display = 'none';
            document.getElementById('appSection').style.display = 'block';
            document.getElementById('userProfile').style.display = 'flex';
            applyRoleAccess(currentUser);
        }
    } catch (err) {
        console.log('No existing session found or server offline.');
    }
}

function showStatus(message, isError = false) {
    const box = document.getElementById('statusMessage');
    box.style.display = 'block';
    box.className = 'alert ' + (isError ? 'alert-danger' : 'alert-success');
    box.innerText = message;
    setTimeout(() => { box.style.display = 'none'; }, 6000);
}

function switchTab(tabId, btn) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    if (btn) {
        btn.classList.add('active');
    } else {
        const matchingBtn = document.getElementById('nav-' + tabId);
        if (matchingBtn) matchingBtn.classList.add('active');
    }
    const target = document.getElementById('tab-' + tabId);
    if (target) {
        target.classList.add('active');
    }
    if (tabId === 'notifications') {
        loadNotifications();
    }
}

// 1. User Authentication (Session & Cookie based)
async function handleLogin(e) {
    e.preventDefault();
    const u = document.getElementById('loginUsername').value;
    const p = document.getElementById('loginPassword').value;

    try {
        const res = await fetch('/api/auth', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `username=${encodeURIComponent(u)}&password=${encodeURIComponent(p)}`
        });

        const data = await res.json();
        if (data.success) {
            currentUser = data.user;
            document.getElementById('loginSection').style.display = 'none';
            document.getElementById('appSection').style.display = 'block';
            document.getElementById('userProfile').style.display = 'flex';
            applyRoleAccess(currentUser);
            showStatus(`Welcome, ${currentUser.fullName}! Active session established.`);
        } else {
            const alertBox = document.getElementById('loginAlert');
            alertBox.style.display = 'block';
            alertBox.innerText = data.message || 'Login failed. Please check your credentials.';
        }
    } catch (err) {
        console.error('Login error:', err);
        const alertBox = document.getElementById('loginAlert');
        alertBox.style.display = 'block';
        alertBox.innerText = 'Unable to connect to server. Please try again.';
    }
}

function applyRoleAccess(user) {
    if (!user) return;
    const role = (user.role || '').toUpperCase();
    const userNameRole = document.getElementById('userNameRole');
    const userRoleBadge = document.getElementById('userRoleBadge');
    const roleWelcomeBanner = document.getElementById('roleWelcomeBanner');
    const roleBannerIcon = document.getElementById('roleBannerIcon');
    const roleBannerTitle = document.getElementById('roleBannerTitle');
    const roleBannerDesc = document.getElementById('roleBannerDesc');
    const roleBannerPill = document.getElementById('roleBannerPill');
    const roleBannerScope = document.getElementById('roleBannerScope');

    const navBook = document.getElementById('nav-book');
    const navSearch = document.getElementById('nav-search');
    const navBilling = document.getElementById('nav-billing');
    const navReports = document.getElementById('nav-reports');
    const navNotifs = document.getElementById('nav-notifications');
    const navHelp = document.getElementById('nav-help');

    const btnRevenue = document.getElementById('btn-report-revenue');
    const revenueNotice = document.getElementById('revenueRestrictedNotice');

    if (userNameRole) {
        userNameRole.innerText = user.fullName;
    }

    if (roleWelcomeBanner) {
        roleWelcomeBanner.className = 'role-banner role-' + (role === 'ADMIN' ? 'admin' : (role === 'DENTIST' ? 'dentist' : 'receptionist'));
    }
    if (userRoleBadge) {
        userRoleBadge.innerText = role;
        userRoleBadge.className = 'role-badge role-' + (role === 'ADMIN' ? 'admin' : (role === 'DENTIST' ? 'dentist' : 'receptionist'));
    }

    if (role === 'DENTIST') {
        // ==========================================
        // 🦷 DENTIST (CLINICAL SURGEON) DASHBOARD
        // ==========================================
        if (roleBannerIcon) roleBannerIcon.innerText = '🩺';
        if (roleBannerTitle) roleBannerTitle.innerText = `${user.fullName} — Clinical Portal`;
        if (roleBannerDesc) roleBannerDesc.innerText = 'Welcome Doctor! Review your daily scheduled patient queue, diagnostic histories, and personal consultation workload.';
        if (roleBannerPill) roleBannerPill.innerText = 'Dental Surgeon Clearance';
        if (roleBannerScope) roleBannerScope.innerText = 'Clinical & Diagnostic Access (Cashier & Registration Locked)';

        // Restrict front-desk tabs
        if (navBook) navBook.style.display = 'none';
        if (navBilling) navBilling.style.display = 'none';
        if (navNotifs) navNotifs.style.display = 'none';

        // Tailor permitted tabs
        if (navSearch) {
            navSearch.style.display = 'inline-block';
            navSearch.innerHTML = '📋 My Schedule & Appointments';
        }
        if (navReports) {
            navReports.style.display = 'inline-block';
            navReports.innerHTML = '📊 Clinical Workload';
        }
        if (navHelp) navHelp.style.display = 'inline-block';

        // In Reports tab: hide revenue
        if (btnRevenue) btnRevenue.style.display = 'none';
        if (revenueNotice) revenueNotice.style.display = 'none';

        // Auto-switch to search/schedule tab
        switchTab('search', navSearch);

    } else if (role === 'RECEPTIONIST') {
        // ==========================================
        // 📋 RECEPTIONIST (FRONT-DESK) DASHBOARD
        // ==========================================
        if (roleBannerIcon) roleBannerIcon.innerText = '📋';
        if (roleBannerTitle) roleBannerTitle.innerText = 'Front Desk Receptionist Operations Portal';
        if (roleBannerDesc) roleBannerDesc.innerText = 'Register incoming patients, verify schedule availability, issue official payment receipts, and dispatch patient SMS alerts.';
        if (roleBannerPill) roleBannerPill.innerText = 'Front Desk Clearance';
        if (roleBannerScope) roleBannerScope.innerText = 'Booking, Invoicing & Patient Queue (Executive Revenue Locked)';

        // Enable front-desk tabs
        if (navBook) {
            navBook.style.display = 'inline-block';
            navBook.innerHTML = '📅 Book Appointment';
        }
        if (navSearch) {
            navSearch.style.display = 'inline-block';
            navSearch.innerHTML = '🔍 Search Appointments';
        }
        if (navBilling) {
            navBilling.style.display = 'inline-block';
            navBilling.innerHTML = '💳 Billing & Receipts';
        }
        if (navNotifs) {
            navNotifs.style.display = 'inline-block';
            navNotifs.innerHTML = '📲 SMS & Alerts';
        }
        if (navHelp) navHelp.style.display = 'inline-block';
        if (navReports) {
            navReports.style.display = 'inline-block';
            navReports.innerHTML = '📊 Daily Patient Queue';
        }

        // In Reports tab: restrict revenue
        if (btnRevenue) btnRevenue.style.display = 'none';
        if (revenueNotice) revenueNotice.style.display = 'none';

        // Auto-switch to book tab
        switchTab('book', navBook);

    } else {
        // ==========================================
        // 👑 SYSTEM ADMINISTRATOR DASHBOARD
        // ==========================================
        if (roleBannerIcon) roleBannerIcon.innerText = '👑';
        if (roleBannerTitle) roleBannerTitle.innerText = 'System Administrator Control Center';
        if (roleBannerDesc) roleBannerDesc.innerText = 'Complete administrative control over all clinical booking, cashier billing, patient medical records, and executive financial revenue reports.';
        if (roleBannerPill) roleBannerPill.innerText = 'Full Executive Clearance';
        if (roleBannerScope) roleBannerScope.innerText = 'All Clinic Modules Unlocked';

        // Show all navigation tabs
        if (navBook) {
            navBook.style.display = 'inline-block';
            navBook.innerHTML = '📅 Book Appointment';
        }
        if (navSearch) {
            navSearch.style.display = 'inline-block';
            navSearch.innerHTML = '🔍 Search Appointments';
        }
        if (navBilling) {
            navBilling.style.display = 'inline-block';
            navBilling.innerHTML = '💳 Billing & Receipts';
        }
        if (navReports) {
            navReports.style.display = 'inline-block';
            navReports.innerHTML = '📊 Decision Reports (All)';
        }
        if (navNotifs) {
            navNotifs.style.display = 'inline-block';
            navNotifs.innerHTML = '📲 SMS & Alerts';
        }
        if (navHelp) navHelp.style.display = 'inline-block';

        // In Reports tab: enable revenue
        if (btnRevenue) btnRevenue.style.display = 'inline-block';
        if (revenueNotice) revenueNotice.style.display = 'none';

        switchTab('book', navBook);
    }
}

async function logout() {
    try {
        await fetch('/api/auth?action=logout');
    } catch (e) {}
    currentUser = null;
    document.getElementById('loginSection').style.display = 'block';
    document.getElementById('appSection').style.display = 'none';
    document.getElementById('userProfile').style.display = 'none';
}

// 2. Book Appointment (triggers automated SMS dispatch)
async function handleBookAppointment(e) {
    e.preventDefault();
    const params = new URLSearchParams();
    params.append('patientName', document.getElementById('patientName').value);
    params.append('address', document.getElementById('patientAddress').value);
    params.append('contact', document.getElementById('patientContact').value);
    params.append('dentistName', document.getElementById('dentistSelect').value);
    params.append('treatmentName', document.getElementById('treatmentSelect').value);
    params.append('appointmentDate', document.getElementById('appointmentDate').value);
    params.append('appointmentTime', document.getElementById('appointmentTime').value);

    try {
        const res = await fetch('/api/appointments', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: params.toString()
        });

        const data = await res.json();
        if (data.success) {
            showStatus(`Appointment successfully booked! Assigned Number: ${data.appointment.appointmentNumber} | 📲 SMS confirmation dispatched to patient!`);
            document.getElementById('bookingForm').reset();
        } else {
            showStatus(`Booking Failed: ${data.message}`, true);
        }
    } catch (err) {
        showStatus('Network error occurred.', true);
    }
}

// 3. Search Appointment (supports Appointment No, Patient Name, or Phone)
async function searchAppointment(queryOverride) {
    const q = (queryOverride || document.getElementById('searchAppNo').value).trim();
    const container = document.getElementById('appointmentResultCard');
    const tableBox = document.getElementById('appointmentsTableContainer');
    if (!q) {
        alert('Please enter an appointment number, patient name, or phone number.');
        return;
    }

    tableBox.innerHTML = '';
    container.style.display = 'none';

    try {
        const res = await fetch(`/api/appointments?q=${encodeURIComponent(q)}`);
        if (!res.ok) {
            container.style.display = 'block';
            container.innerHTML = `<p style="color: #ef4444; font-weight: 600;">Error searching for '${q}'.</p>`;
            return;
        }

        const list = await res.json();
        if (!list || list.length === 0) {
            container.style.display = 'block';
            container.innerHTML = `<p style="color: #ef4444; font-weight: 600;">No appointments found matching '${q}'.</p>`;
            return;
        }

        if (list.length === 1) {
            renderAppointmentCard(list[0]);
        } else {
            // Multiple results found! Render a table of matches
            let html = `
                <div style="margin-bottom: 12px;">
                    <h4>Found ${list.length} matching appointment(s) for "${q}":</h4>
                </div>
                <table>
                    <thead>
                        <tr><th>App No</th><th>Patient Name</th><th>Contact</th><th>Dentist</th><th>Date & Time</th><th>Status</th><th>Action</th></tr>
                    </thead>
                    <tbody>`;
            list.forEach(a => {
                let statusColor = '#0284c7';
                if (a.status === 'IN_TREATMENT') statusColor = '#d97706';
                if (a.status === 'COMPLETED') statusColor = '#16a34a';
                if (a.status === 'CANCELLED') statusColor = '#dc2626';

                html += `<tr>
                    <td><strong>${a.appointmentNumber}</strong></td>
                    <td>${a.patientName}</td>
                    <td>${a.patientContact}</td>
                    <td>${a.dentistName}</td>
                    <td>${a.appointmentDate} <small style="color:#64748b;">(${a.appointmentTime})</small></td>
                    <td><span class="user-badge" style="background:${statusColor};color:#fff;font-size:11px;">${a.status}</span></td>
                    <td><button class="btn-action-view" onclick="loadSingleAppointment('${a.appointmentNumber}')">👁️ View</button></td>
                </tr>`;
            });
            html += '</tbody></table>';
            tableBox.innerHTML = html;
        }
    } catch (e) {
        container.style.display = 'block';
        container.innerHTML = `<p style="color: #ef4444;">Search request failed. Please check connection.</p>`;
    }
}

async function loadSingleAppointment(appNo) {
    try {
        const res = await fetch(`/api/appointments?appNo=${encodeURIComponent(appNo)}`);
        if (res.ok) {
            const app = await res.json();
            renderAppointmentCard(app);
            // Scroll smoothly to card
            document.getElementById('appointmentResultCard').scrollIntoView({ behavior: 'smooth' });
        }
    } catch (e) {
        alert('Failed to load appointment details.');
    }
}

function renderAppointmentCard(app) {
    const container = document.getElementById('appointmentResultCard');
    container.style.display = 'block';

    let statusBadge = '';
    if (app.status === 'IN_TREATMENT') {
        statusBadge = '<span class="user-badge" style="background:#d97706; color:#fff; font-weight:700;">🩺 IN TREATMENT</span>';
    } else if (app.status === 'COMPLETED') {
        statusBadge = '<span class="user-badge" style="background:#16a34a; color:#fff; font-weight:700;">✅ COMPLETED</span>';
    } else if (app.status === 'CANCELLED') {
        statusBadge = '<span class="user-badge" style="background:#dc2626; color:#fff; font-weight:700;">❌ CANCELLED</span>';
    } else {
        statusBadge = '<span class="user-badge" style="background:#0284c7; color:#fff; font-weight:700;">📅 BOOKED</span>';
    }

    const role = currentUser ? (currentUser.role || '').toUpperCase() : '';
    let actionButtons = '';

    // Clinical Lifecycle Actions (Dentist & Admin)
    if (role === 'DENTIST' || role === 'ADMIN') {
        if (app.status === 'BOOKED') {
            actionButtons += `<button class="btn" style="background:#d97706; color:#fff;" onclick="updateAppointmentStatus('${app.appointmentNumber}', 'IN_TREATMENT')">🩺 Call Patient (Start Treatment)</button> `;
        } else if (app.status === 'IN_TREATMENT') {
            actionButtons += `<button class="btn" style="background:#16a34a; color:#fff;" onclick="updateAppointmentStatus('${app.appointmentNumber}', 'COMPLETED')">✅ Mark Treatment Completed</button> `;
        }
    }

    // Front-Desk Actions (Receptionist & Admin)
    if (role === 'RECEPTIONIST' || role === 'ADMIN') {
        if (app.status !== 'CANCELLED' && app.status !== 'COMPLETED') {
            actionButtons += `<button class="btn btn-outline" onclick="toggleRescheduleBox('${app.appointmentNumber}')">🗓️ Reschedule</button> `;
            actionButtons += `<button class="btn btn-danger" onclick="cancelAppointment('${app.appointmentNumber}')">❌ Cancel</button> `;
        }
        if (app.status === 'COMPLETED') {
            actionButtons += `<button class="btn btn-primary" onclick="proceedToBill('${app.appointmentNumber}')">💳 Process Billing & Receipt</button> `;
        }
    }

    container.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #e2e8f0; padding-bottom:10px; margin-bottom:12px;">
            <h4>Appointment Record: ${app.appointmentNumber}</h4>
            <div>${statusBadge}</div>
        </div>
        <table>
            <tr><th style="width:200px;">Patient Name</th><td><strong>${app.patientName}</strong></td></tr>
            <tr><th>Contact Number</th><td>${app.patientContact}</td></tr>
            <tr><th>Address</th><td>${app.patientAddress}</td></tr>
            <tr><th>Consulting Dentist</th><td>${app.dentistName}</td></tr>
            <tr><th>Treatment Procedure</th><td>${app.treatmentName}</td></tr>
            <tr><th>Scheduled Date & Time</th><td><strong>${app.appointmentDate}</strong> at <strong>${app.appointmentTime}</strong></td></tr>
            <tr><th>Current Status</th><td>${statusBadge}</td></tr>
        </table>

        <!-- Lifecycle Actions -->
        <div style="margin-top: 18px; display:flex; gap:10px; flex-wrap:wrap; align-items:center;">
            ${actionButtons}
        </div>

        <!-- Inline Reschedule Box -->
        <div id="rescheduleBox_${app.appointmentNumber}" style="display:none; margin-top:15px; padding:15px; background:#f8fafc; border:1px solid #cbd5e1; border-radius:8px;">
            <h5 style="margin-bottom:8px; color:#0f172a;">🗓️ Reschedule Appointment ${app.appointmentNumber}</h5>
            <div style="display:flex; gap:12px; flex-wrap:wrap; margin-bottom:10px;">
                <div>
                    <label style="font-size:12px; font-weight:600;">New Date:</label><br>
                    <input type="date" id="newDate_${app.appointmentNumber}" value="${app.appointmentDate}" style="padding:6px 10px; border:1px solid #cbd5e1; border-radius:6px;">
                </div>
                <div>
                    <label style="font-size:12px; font-weight:600;">New Time Slot:</label><br>
                    <select id="newTime_${app.appointmentNumber}" style="padding:6px 10px; border:1px solid #cbd5e1; border-radius:6px;">
                        <option value="09:00 AM">09:00 AM</option>
                        <option value="09:45 AM">09:45 AM</option>
                        <option value="10:30 AM">10:30 AM</option>
                        <option value="11:15 AM">11:15 AM</option>
                        <option value="02:00 PM">02:00 PM</option>
                        <option value="02:45 PM">02:45 PM</option>
                        <option value="03:30 PM">03:30 PM</option>
                        <option value="04:15 PM">04:15 PM</option>
                    </select>
                </div>
            </div>
            <button class="btn btn-primary" style="padding:6px 14px; font-size:13px;" onclick="submitReschedule('${app.appointmentNumber}')">Confirm Reschedule</button>
            <button class="btn btn-outline" style="padding:6px 14px; font-size:13px;" onclick="toggleRescheduleBox('${app.appointmentNumber}')">Cancel</button>
        </div>
    `;

    const timeSelect = document.getElementById(`newTime_${app.appointmentNumber}`);
    if (timeSelect && app.appointmentTime) {
        timeSelect.value = app.appointmentTime.trim();
    }
}

function toggleRescheduleBox(appNo) {
    const box = document.getElementById('rescheduleBox_' + appNo);
    if (box) {
        box.style.display = (box.style.display === 'none') ? 'block' : 'none';
    }
}

async function submitReschedule(appNo) {
    const newDate = document.getElementById('newDate_' + appNo).value;
    const newTime = document.getElementById('newTime_' + appNo).value;

    if (!newDate || !newTime) {
        alert('Please select both a new date and time slot.');
        return;
    }

    try {
        const res = await fetch('/api/appointments?action=reschedule', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `appNo=${encodeURIComponent(appNo)}&appointmentDate=${encodeURIComponent(newDate)}&appointmentTime=${encodeURIComponent(newTime)}`
        });
        const data = await res.json();
        if (data.success) {
            showStatus(`Appointment ${appNo} successfully rescheduled to ${newDate} at ${newTime}! SMS dispatched.`);
            renderAppointmentCard(data.appointment);
        } else {
            alert(data.message || 'Rescheduling failed.');
        }
    } catch (err) {
        alert('Failed to contact server for rescheduling.');
    }
}

async function updateAppointmentStatus(appNo, newStatus) {
    try {
        const res = await fetch('/api/appointments?action=status', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `appNo=${encodeURIComponent(appNo)}&status=${encodeURIComponent(newStatus)}`
        });
        const data = await res.json();
        if (data.success) {
            showStatus(`Appointment ${appNo} status updated to ${newStatus}!`);
            renderAppointmentCard(data.appointment);
        } else {
            alert(data.message || 'Status update failed.');
        }
    } catch (err) {
        alert('Failed to contact server for status update.');
    }
}

function proceedToBill(appNo) {
    const navBilling = document.getElementById('nav-billing');
    switchTab('billing', navBilling);
    const billInput = document.getElementById('billAppNo');
    if (billInput) {
        billInput.value = appNo;
        generateBill();
    }
}

async function cancelAppointment(appNo) {
    if (!confirm(`Are you sure you want to cancel appointment ${appNo}?`)) return;
    try {
        const res = await fetch(`/api/appointments?appNo=${encodeURIComponent(appNo)}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showStatus(`Appointment ${appNo} cancelled successfully. Cancellation SMS sent.`);
            loadSingleAppointment(appNo);
        } else {
            alert(data.message || 'Cancellation failed.');
        }
    } catch (e) {
        alert('Failed to cancel appointment.');
    }
}

async function loadAllAppointments() {
    const res = await fetch('/api/appointments');
    const apps = await res.json();
    const box = document.getElementById('appointmentsTableContainer');
    const resultCard = document.getElementById('appointmentResultCard');
    resultCard.style.display = 'none';

    if (apps.length === 0) {
        box.innerHTML = '<p>No appointments found in database.</p>';
        return;
    }
    let html = `<table>
        <thead>
            <tr><th>App No</th><th>Patient</th><th>Contact</th><th>Dentist</th><th>Date & Time</th><th>Status</th><th>Action</th></tr>
        </thead>
        <tbody>`;
    apps.forEach(a => {
        let statusColor = '#0284c7';
        if (a.status === 'IN_TREATMENT') statusColor = '#d97706';
        if (a.status === 'COMPLETED') statusColor = '#16a34a';
        if (a.status === 'CANCELLED') statusColor = '#dc2626';

        html += `<tr>
            <td><strong>${a.appointmentNumber}</strong></td>
            <td>${a.patientName}</td>
            <td>${a.patientContact}</td>
            <td>${a.dentistName}</td>
            <td>${a.appointmentDate} <small style="color:#64748b;">(${a.appointmentTime})</small></td>
            <td><span class="user-badge" style="background:${statusColor};color:#fff;font-size:11px;">${a.status}</span></td>
            <td><button class="btn-action-view" onclick="loadSingleAppointment('${a.appointmentNumber}')">👁️ View</button></td>
        </tr>`;
    });
    html += '</tbody></table>';
    box.innerHTML = html;
}

// 4. Calculate & Print Bill (triggers electronic receipt email)
async function generateBill() {
    const appNo = document.getElementById('billAppNo').value.trim();
    const box = document.getElementById('billResultBox');
    const body = document.getElementById('receiptBody');
    let billAlert = document.getElementById('billAlert');
    if (!billAlert) {
        billAlert = document.createElement('div');
        billAlert.id = 'billAlert';
        billAlert.className = 'alert alert-danger';
        box.parentNode.insertBefore(billAlert, box);
    }
    billAlert.style.display = 'none';

    if (!appNo) {
        billAlert.style.display = 'block';
        billAlert.innerText = 'Please enter an appointment number.';
        return;
    }

    try {
        const res = await fetch(`/api/billing?appNo=${encodeURIComponent(appNo)}`);
        if (res.ok) {
            const bill = await res.json();
            box.style.display = 'block';
            body.innerHTML = `
                <p><strong>Receipt No   :</strong> ${bill.billNumber}</p>
                <p><strong>Appoint. No  :</strong> ${bill.appointmentNumber}</p>
                <p><strong>Patient Name :</strong> ${bill.patientName}</p>
                <p><strong>Consultant   :</strong> ${bill.dentistName}</p>
                <hr style="margin: 10px 0;">
                <p><strong>Consultation Fee :</strong> LKR ${bill.consultationFee.toFixed(2)}</p>
                <p><strong>${bill.treatmentName} :</strong> LKR ${bill.treatmentCost.toFixed(2)}</p>
                <hr style="margin: 10px 0;">
                <h3 style="color:#0284c7;">TOTAL PAYABLE: LKR ${bill.totalAmount.toFixed(2)}</h3>
                <p style="margin-top: 8px;"><strong>Status:</strong> ${bill.paymentStatus}</p>
                <p style="margin-top: 8px; color: #16a34a; font-weight: 600;">📧 Electronic receipt email dispatched to patient.</p>
            `;
        } else {
            box.style.display = 'none';
            const errData = await res.json().catch(() => ({}));
            billAlert.style.display = 'block';
            billAlert.innerText = 'Billing Failed: ' + (errData.error || ('Cannot generate bill for appointment ' + appNo));
        }
    } catch (e) {
        box.style.display = 'none';
        billAlert.style.display = 'block';
        billAlert.innerText = 'Billing Failed: Network or server error.';
    }
}

// 5. Reports
async function loadScheduleReport() {
    const res = await fetch('/api/reports?type=schedule');
    const data = await res.json();
    let html = '<h4>📋 Tomorrow Schedule Report</h4>';
    if (data.length === 0) {
        html += '<p>No appointments booked for tomorrow.</p>';
    } else {
        html += '<table><thead><tr><th>App No</th><th>Patient</th><th>Doctor</th><th>Treatment</th><th>Time</th></tr></thead><tbody>';
        data.forEach(a => {
            html += `<tr><td>${a.appointmentNumber}</td><td>${a.patientName}</td><td>${a.dentistName}</td><td>${a.treatmentName}</td><td>${a.appointmentTime}</td></tr>`;
        });
        html += '</tbody></table>';
    }
    document.getElementById('reportContent').innerHTML = html;
}

async function loadRevenueReport() {
    try {
        const res = await fetch('/api/reports?type=revenue');
        if (res.status === 403) {
            document.getElementById('reportContent').innerHTML = `
                <div class="alert alert-danger">
                    <strong>🔒 Access Denied:</strong> Executive financial revenue breakdowns are restricted to System Administrators (ADMIN).
                </div>`;
            return;
        }
        const data = await res.json();
        let html = '<h4>💰 Revenue Breakdown by Dental Procedure</h4><table><thead><tr><th>Procedure</th><th>Total Invoiced (LKR)</th></tr></thead><tbody>';
        let total = 0;
        for (let k in data) {
            html += `<tr><td>${k}</td><td>LKR ${data[k].toFixed(2)}</td></tr>`;
            total += data[k];
        }
        html += `<tr style="font-weight:700; background:#e0f2fe;"><td>TOTAL CLINIC REVENUE</td><td>LKR ${total.toFixed(2)}</td></tr></tbody></table>`;
        document.getElementById('reportContent').innerHTML = html;
    } catch (e) {
        document.getElementById('reportContent').innerHTML = '<p style="color:#ef4444;">Failed to load revenue report.</p>';
    }
}

async function loadWorkloadReport() {
    const res = await fetch('/api/reports?type=workload');
    const data = await res.json();
    let html = '<h4>👨‍⚕️ Dentist Consultation Workload</h4><table><thead><tr><th>Dentist Name</th><th>Total Patient Visits</th></tr></thead><tbody>';
    for (let k in data) {
        html += `<tr><td>${k}</td><td>${data[k]} patient(s)</td></tr>`;
    }
    html += '</tbody></table>';
    document.getElementById('reportContent').innerHTML = html;
}

// 6. Automated SMS & Email Notification Audit Log
async function loadNotifications() {
    const container = document.getElementById('notificationsContainer');
    try {
        const res = await fetch('/api/notifications');
        const logs = await res.json();
        if (!logs || logs.length === 0) {
            container.innerHTML = '<p style="color: #64748b;">No SMS or Email notifications logged yet. Book an appointment or generate a bill to trigger automated dispatches.</p>';
            return;
        }
        let html = '<div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:12px; max-height:350px; overflow-y:auto; font-family:monospace; font-size:12.5px;">';
        logs.slice().reverse().forEach(log => {
            let color = log.includes('SMS') ? '#0284c7' : '#16a34a';
            html += `<div style="padding:6px 0; border-bottom:1px dashed #cbd5e1; color:${color};"><strong>${log}</strong></div>`;
        });
        html += '</div>';
        container.innerHTML = html;
    } catch (e) {
        container.innerHTML = '<p style="color:#ef4444;">Unable to load notification logs.</p>';
    }
}
