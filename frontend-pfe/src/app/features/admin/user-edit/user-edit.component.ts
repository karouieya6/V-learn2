import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { NgSelectModule } from '@ng-select/ng-select';
import { AdminDashboardService } from '../services/admin-dashboard.service';
import { Router } from '@angular/router';
import { NgbAlertModule } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-user-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgSelectModule,NgbAlertModule],
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.scss']
})
export class UserEditComponent implements OnInit {
  userData: any;
  userForm!: FormGroup;
  profileImageUrl: string | null = null;
  defaultAvatar = 'https://via.placeholder.com/100';
  successMessage: string | null = null;

  roleOptions = [
    { label: 'User', value: 'USER' },
    { label: 'Instructor', value: 'INSTRUCTOR' },
    { label: 'Admin', value: 'ADMIN' }
  ];

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private http: HttpClient,
    private dashboardService: AdminDashboardService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // 1. Initialisation vide (évite l'erreur Angular NG01052)
    this.userForm = this.fb.group({
      firstName: [''],
      lastName: [''],
      username: [''],
      email: [''],
      phone: [''],
      roles: [[]]
    });
  
    // 2. Chargement des données si ID valide
    const userId = Number(this.route.snapshot.paramMap.get('id'));
    if (!userId) return;
  
    this.dashboardService.getUserDétails(userId).subscribe({
      next: (user) => {
        this.userData = user;
        this.initForm(user); // remplissage avec les vraies données
      },
      error: (err) => console.error('Error loading user', err)
    });
  }

  initForm(user: any) {
    this.userForm = this.fb.group({
      firstName: [user.firstName || ''],
      lastName: [user.lastName || ''],
      username: [user.username || ''],
      email: [user.email || ''],
      phone: [user.phone || ''],
      roles: [user.roles || []]
    });

    this.profileImageUrl = user.profileImageUrl || null;
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    // implement image upload if needed
  }

  onSave() {
    const userId = this.userData?.id;
    if (!userId) return;
  
    const { username, email, roles } = this.userForm.value;
    const payload = { username, email, roles };
  
    this.dashboardService.updateUser(userId, payload).subscribe({
      next: () => {
        this.successMessage = '✅ User updated successfully!';
        setTimeout(() => {
          this.successMessage = null;
          this.router.navigate(['/admin/students']);
        }, 2000);
      },
      error: (err) => {
        if (err.status === 200) {
          this.successMessage = '✅ User updated';
          setTimeout(() => {
            this.successMessage = null;
            this.router.navigate(['/admin/students']);
          }, 2000);
        } else {
          console.error('Update failed', err);
        }
      }
    });
  }
}
