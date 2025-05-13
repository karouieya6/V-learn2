import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminCounterStateComponent } from './admin-counter-state.component';

describe('AdminCounterStateComponent', () => {
  let component: AdminCounterStateComponent;
  let fixture: ComponentFixture<AdminCounterStateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCounterStateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminCounterStateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
