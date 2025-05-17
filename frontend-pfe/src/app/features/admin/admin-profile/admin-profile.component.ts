import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { UserService } from '../../../services/user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-admin-profile',
  standalone: true,
  templateUrl: './admin-profile.component.html',
  styleUrls: ['./admin-profile.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule]
})
export class AdminProfileComponent implements OnInit {
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  userId: number = 0;

  profileImageUrl: string | null = null;
  selectedFile: File | null = null;
  defaultAvatar = 'assets/img/user/userprofile.png';

  constructor(private fb: FormBuilder, private userService: UserService) {}

  ngOnInit() {
    this.profileForm = this.fb.group({
      firstName: [''],
      lastName: [''],
      username: [''],
      email: [''],
      phone: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: [''],
      newPassword: [''],
      confirmPassword: ['']
    });

    this.userService.getProfile().subscribe({
      next: (profile) => {
        this.profileForm.patchValue(profile);
        this.userId = profile.id;
        this.profileImageUrl = this.userService.getProfileImageUrl(profile.imageUrl || this.defaultAvatar);
        console.log("this.profileImageUrl",this.profileImageUrl);
        
      },
      error: () => this.showError('Failed to load profile.')
    });
  }

  onSave() {
    if (this.profileForm.invalid) return;

    this.userService.updateProfile(this.profileForm.value).subscribe({
      next: (updatedUser) => {
        localStorage.setItem('user', JSON.stringify(updatedUser));
        if (this.selectedFile) {
          const formData = new FormData();
          formData.append('file', this.selectedFile);

          this.userService.uploadProfilePicture(this.userId, formData).subscribe({
            next: (imageUrl: string) => {
              this.profileImageUrl = imageUrl;
              this.selectedFile = null;
              this.showSuccess('Profile & picture updated!');
            },
            error: () => this.showWarning("Profile updated but image failed.")
          });
        } else {
          this.showSuccess("Profile updated successfully!");
        }
      },
      error: () => this.showError("Profile update failed.")
    });
  }

  onPasswordChange() {
    const { currentPassword, newPassword, confirmPassword } = this.passwordForm.value;

    if (!currentPassword || !newPassword || !confirmPassword) {
      return this.showWarning("Please fill in all password fields.");
    }

    if (newPassword !== confirmPassword) {
      return this.showWarning("Passwords do not match.");
    }

    this.userService.changePassword({
      oldPassword: currentPassword,
      newPassword: newPassword
    }).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Password changed',
          text: 'Please log in again.',
          timer: 3000,
          showConfirmButton: false
        });
        this.passwordForm.reset();
        this.userService.logout().subscribe();
      },
      error: (err) => {
        if (err.status === 400) {
          this.showError('Incorrect current password.');
        } else if (err.status === 403) {
          this.showError('Session expired. Please log in again.');
          this.userService.logout().subscribe();
        } else {
          this.showError('An error occurred. Please try again.');
        }
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files?.[0];
    if (!file) return;

    this.selectedFile = file;

    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.profileImageUrl = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  private showSuccess(message: string) {
    Swal.fire({
      icon: 'success',
      title: message,
      toast: true,
      position: 'top-end',
      timer: 3000,
      showConfirmButton: false
    });
  }

  private showError(message: string) {
    Swal.fire({
      icon: 'error',
      title: message,
      toast: true,
      position: 'top-end',
      timer: 3000,
      showConfirmButton: false
    });
  }

  private showWarning(message: string) {
    Swal.fire({
      icon: 'warning',
      title: message,
      toast: true,
      position: 'top-end',
      timer: 3000,
      showConfirmButton: false
    });
  }
}
