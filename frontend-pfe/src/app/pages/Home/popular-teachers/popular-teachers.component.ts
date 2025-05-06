import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
@Component({
  standalone: true,
  selector: 'app-popular-teachers',
  templateUrl: './popular-teachers.component.html',
  styleUrls: ['./popular-teachers.component.scss'],
  imports: [
    CommonModule,
    RouterModule
  ]
})
export class PopularTeachersComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
