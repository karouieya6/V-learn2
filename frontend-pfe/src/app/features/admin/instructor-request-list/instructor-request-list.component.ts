import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbPaginationModule } from '@ng-bootstrap/ng-bootstrap';
import { AdminDashboardService } from '../services/admin-dashboard.service';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { NgbAlertModule } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-instructor-request-list',
  standalone: true,
  imports: [CommonModule, FormsModule, NgbPaginationModule,NgbAlertModule],
  templateUrl: './instructor-request-list.component.html',
})
export class InstructorRequestListComponent implements OnInit {
  requests: any[] = [];
  page: number = 1;
  pageSize: number = 10;
  totalPages: number = 0;
  searchTerm: string = '';
  successMessage: string | null = null;
  errorMessage: string | null = null;
  constructor(private dashboardService: AdminDashboardService, private modalService: NgbModal) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests() {
    this.dashboardService.getInstructorRequests().subscribe({
      next: (data) => {
        console.log("DATA", data);
        this.requests = Array.isArray(data) ? data : data.requests;
        this.totalPages = 1;
      },
      error: (err) => console.error('Erreur de chargement', err)
    });
  }

  onSearchChange() {
    this.page = 1;
    this.loadRequests();
  }

  onPageChange(newPage: number) {
    this.page = newPage;
    this.loadRequests();
  }
  openConfirmDialog(action: 'accept' | 'reject', userId: number, username: string): void {
    const modalRef = this.modalService.open(ConfirmDialogComponent, {
      backdrop: 'static',
      centered: true,
      size: 'sm',
      windowClass: 'confirm-modal'
    });
  
    modalRef.componentInstance.message =
      action === 'accept'
        ? `Are you sure you want to accept ${username} as an instructor?`
        : `Are you sure you want to reject ${username}'s request?`;
  
    modalRef.result.then((confirmed) => {
      if (confirmed) {
        action === 'accept'
          ? this.acceptInstructor(userId)
          : this.rejectInstructor(userId);
      }
    }).catch(() => {});
  }
  
  acceptInstructor(userId: number) {
    this.dashboardService.acceptInstructorRequest(userId).subscribe({
      next: () => {
        this.loadRequests();
        this.successMessage = 'Instructor request accepted successfully!';
        setTimeout(() => (this.successMessage = null), 3000);
      },
      error: (err) => console.error('Accept failed', err)
    });
  }
  
  rejectInstructor(userId: number) {
    this.dashboardService.rejectInstructorRequest(userId).subscribe({
      next: () => {
        this.loadRequests();
        this.errorMessage = 'Instructor request rejected.';
        setTimeout(() => (this.errorMessage = null), 3000);
      },
      error: (err) => console.error('Reject failed', err)
    });
  }
}
