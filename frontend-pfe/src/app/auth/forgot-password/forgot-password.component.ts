import { Component } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
@Component({
  selector: 'app-forgot-password',
  standalone:true,
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss'],
  imports: [CommonModule, FormsModule]
})
export class ForgotPasswordComponent {
  email: string = '';
  successMessage = '';
  errorMessage = '';

  constructor(private http: HttpClient) {}

  submit() {
    if (!this.email.trim()) {
      this.errorMessage = 'Veuillez entrer votre email.';
      return;
    }

    const url = 'http://localhost:8080/userservice/auth/forgot-password';
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });

    this.http.post(url, { email: this.email.trim() }, { headers }).subscribe({
      next: () => {
        this.successMessage = 'Un lien de réinitialisation a été envoyé à votre email.';
        this.errorMessage = '';
        this.email = '';
      },
      error: (err) => {
        console.error('Forgot password error', err);
        this.successMessage = '';
        this.errorMessage = 'Erreur. Veuillez vérifier votre email.';
      }
    });
  }
}
