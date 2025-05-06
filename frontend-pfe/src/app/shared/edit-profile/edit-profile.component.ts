import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { UserService } from '../../services/user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  templateUrl: './edit-profile.component.html',
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule],
})
export class EditProfileComponent implements OnInit {
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
        this.profileImageUrl = profile.imageUrl || this.defaultAvatar;
      },
      error: () => this.showError('Erreur lors du chargement du profil')
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
              this.showSuccess('Profil et photo mis à jour !');
            },
            error: () => this.showWarning("Infos mises à jour, mais erreur photo !")
          });
        } else {
          this.showSuccess("Informations mises à jour !");
        }
      },
      error: () => this.showError("Échec de la mise à jour du profil")
    });
    
  }

  onPasswordChange() {
    const { currentPassword, newPassword, confirmPassword } = this.passwordForm.value;

    if (!currentPassword || !newPassword || !confirmPassword) {
      return this.showWarning("Veuillez remplir tous les champs du mot de passe.");
    }

    if (newPassword !== confirmPassword) {
      return this.showWarning("Les mots de passe ne correspondent pas");
    }

    this.userService.changePassword({
      oldPassword: currentPassword,
      newPassword: newPassword
    }).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Mot de passe changé',
          text: 'Reconnectez-vous avec votre nouveau mot de passe',
          timer: 3000,
          showConfirmButton: false
        });
        this.passwordForm.reset();
        this.userService.logout().subscribe();
      },
      error: (err) => {
        if (err.status === 400) {
          this.showError('Ancien mot de passe incorrect');
        } else if (err.status === 403) {
          this.showError('Session expirée. Veuillez vous reconnecter.');
          this.userService.logout().subscribe();
        } else {
          this.showError('Une erreur est survenue. Veuillez réessayer.');
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

  /** 🔔 TOAST HELPERS */
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
