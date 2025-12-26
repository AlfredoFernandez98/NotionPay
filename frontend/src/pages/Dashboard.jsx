import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiFacade from '../util/apiFacade';
import { useAuth } from '../hooks/useAuth';
import { ROUTES } from '../utils/routes';
import {
  DashboardContainer,
  DashboardHeader,
  WelcomeText,
  SubText,
  DashboardGrid,
  Card,
  LargeCard,
  CardHeader,
  CardTitle,
  CardIcon,
  CardContent,
  InfoRow,
  InfoLabel,
  InfoValue,
  StatusBadge,
  DropdownButton,
  DropdownContent,
  DropdownItem,
  EmptyState,
  LoadingSpinner,
  ErrorMessage,
  ActivityItem,
  ActivityType,
  ActivityDate,
  PaymentItem,
  PaymentAmount,
  PaymentDate,
  ReceiptItem,
  ViewButton,
} from './Dashboard.styles';

const Dashboard = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [customerData, setCustomerData] = useState(null);
  const [subscription, setSubscription] = useState(null);
  const [allPlans, setAllPlans] = useState([]);
  const [smsBalance, setSmsBalance] = useState(null);
  const [payments, setPayments] = useState([]);
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [activities, setActivities] = useState([]);
  const [receipts, setReceipts] = useState([]);
  const [error, setError] = useState('');
  const [showSubscriptionDetails, setShowSubscriptionDetails] = useState(false);

  useEffect(() => {
    // Redirect to login if not authenticated
    if (!isAuthenticated) {
      navigate(ROUTES.login);
      return;
    }

    // Fetch dashboard data
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const customerId = apiFacade.getCustomerId();
        
        // Fetch all available plans
        try {
          const plans = await apiFacade.getAllPlans();
          setAllPlans(plans || []);
        } catch (err) {
          console.log('Error fetching plans:', err);
        }
        
        if (customerId) {
          // Fetch customer profile
          const customerProfile = await apiFacade.getCustomerProfile(customerId);
          setCustomerData(customerProfile);
          
          // Fetch subscription data
          try {
            const subscriptionData = await apiFacade.getCustomerSubscription(customerId);
            setSubscription(subscriptionData);
          } catch (err) {
            console.log('No subscription data available');
          }
          
          // Fetch SMS balance
          try {
            const balance = await apiFacade.getSmsBalance(customerId);
            setSmsBalance(balance);
          } catch (err) {
            console.log('No SMS balance data available:', err);
          }

          // Fetch payment methods
          try {
            const paymentMethodData = await apiFacade.getCustomerPaymentMethods(customerId);
            setPaymentMethods(paymentMethodData || []);
          } catch (err) {
            console.log('No payment methods available:', err);
          }

          // Fetch receipts
          try {
            const receiptData = await apiFacade.getCustomerReceipts(customerId);
            setReceipts(receiptData || []);
            
            // Create mock payments from receipts if available
            if (receiptData && receiptData.length > 0) {
              const mockPayments = receiptData.map(receipt => ({
                id: receipt.id,
                amount: receipt.amount,
                description: receipt.description || 'Payment',
                createdAt: receipt.createdAt
              }));
              setPayments(mockPayments);
            }
          } catch (err) {
            console.log('No receipts available:', err);
          }

          // Fetch real activity data
          try {
            console.log('Fetching activities for customer:', customerId);
            const activityData = await apiFacade.getCustomerActivities(customerId);
            console.log('✅ Activities loaded:', activityData);
            console.log('Number of activities:', activityData?.length || 0);
            setActivities(activityData || []);
          } catch (err) {
            console.error('❌ Error fetching activities:', err);
            console.error('Error details:', err.message);
            // Fallback to empty array if fetch fails
            setActivities([]);
          }
        }
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
        setError('Failed to load dashboard data. Please try refreshing the page.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [isAuthenticated, navigate]);

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('da-DK', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatCurrency = (amount) => {
    if (!amount) return '0 kr';
    return `${amount.toFixed(2)} kr`;
  };

  const formatPlanPrice = (priceCents, currency) => {
    if (!priceCents) return '0';
    return `${(priceCents / 100).toFixed(0)} ${currency || 'DKK'}`;
  };

  const isCurrentPlan = (planId) => {
    return subscription?.planId === planId;
  };

  const getCurrentPlanDetails = () => {
    if (!subscription?.planId) return null;
    return allPlans.find(plan => plan.id === subscription.planId);
  };

  const formatActivityType = (type) => {
    const typeMap = {
      'LOGIN': 'Logged in',
      'LOGOUT': 'Logged out',
      'SUBSCRIPTION_CREATED': 'Subscription created',
      'SUBSCRIPTION_UPDATED': 'Subscription updated',
      'SUBSCRIPTION_CANCELLED': 'Subscription cancelled',
      'PAYMENT_SUCCESS': 'Payment successful',
      'PAYMENT_FAILED': 'Payment failed',
      'SMS_PURCHASED': 'SMS credits purchased',
      'PROFILE_UPDATED': 'Profile updated'
    };
    return typeMap[type] || type;
  };

  if (loading) {
    return (
      <DashboardContainer>
        <LoadingSpinner>Loading your dashboard...</LoadingSpinner>
      </DashboardContainer>
    );
  }

  if (error) {
    return (
      <DashboardContainer>
        <ErrorMessage>{error}</ErrorMessage>
      </DashboardContainer>
    );
  }

  return (
    <DashboardContainer>
      <DashboardHeader>
        <WelcomeText>Hej, {customerData?.companyName || 'Velkommen'}!</WelcomeText>
        <SubText>{user?.email || ''}</SubText>
      </DashboardHeader>

      <DashboardGrid>
        {/* Profile Card */}
        <Card>
          <CardHeader>
            <CardTitle>
              Profile
            </CardTitle>
          </CardHeader>
          <CardContent>
            {customerData ? (
              <>
                <InfoRow>
                  <InfoLabel>Company</InfoLabel>
                  <InfoValue>{customerData.companyName}</InfoValue>
                </InfoRow>
                <InfoRow>
                  <InfoLabel>Email</InfoLabel>
                  <InfoValue>{user?.email}</InfoValue>
                </InfoRow>
                <InfoRow>
                  <InfoLabel>Serial Number</InfoLabel>
                  <InfoValue>{customerData.serialNumber}</InfoValue>
                </InfoRow>
                <InfoRow>
                  <InfoLabel>Customer ID</InfoLabel>
                  <InfoValue>#{customerData.id}</InfoValue>
                </InfoRow>
              </>
            ) : (
              <EmptyState>No profile data available</EmptyState>
            )}
          </CardContent>
        </Card>

        {/* Abonnement Card with Dropdown */}
        <Card>
          <CardHeader>
            <CardTitle>
              Abonnement
            </CardTitle>
          </CardHeader>
          <CardContent>
            {subscription ? (
              allPlans.length > 0 ? (
              <>
                <DropdownButton onClick={() => setShowSubscriptionDetails(!showSubscriptionDetails)}>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', flex: 1 }}>
                    <span style={{ fontWeight: '700', fontSize: '1.125rem', color: '#6BB8E8' }}>
                      {subscription.planName || 'No Plan'}
                    </span>
                    {getCurrentPlanDetails() && (
                      <span style={{ fontSize: '0.875rem', color: '#718096', marginTop: '4px' }}>
                        {formatPlanPrice(getCurrentPlanDetails().priceCents, getCurrentPlanDetails().currency)}/{getCurrentPlanDetails().period?.toLowerCase() || 'month'}
                      </span>
                    )}
                  </div>
                  <span style={{ fontSize: '1.25rem' }}>{showSubscriptionDetails ? '▲' : '▼'}</span>
                </DropdownButton>
                
                {showSubscriptionDetails && (
                  <DropdownContent>
                    {/* Current Subscription Info */}
                    <div style={{ 
                      padding: '1rem', 
                      background: '#f0f9ff', 
                      borderRadius: '8px',
                      marginBottom: '1rem',
                      border: '2px solid #6BB8E8'
                    }}>
                      <div style={{ 
                        display: 'flex', 
                        justifyContent: 'space-between', 
                        alignItems: 'center',
                        marginBottom: '0.75rem'
                      }}>
                        <span style={{ fontWeight: '600', color: '#2d3748' }}>Your Current Plan</span>
                        <StatusBadge status={subscription.status}>
                          {subscription.status}
                        </StatusBadge>
                      </div>
                      <InfoRow style={{ marginBottom: '0.5rem' }}>
                        <InfoLabel>Start Date</InfoLabel>
                        <InfoValue>{formatDate(subscription.startDate)}</InfoValue>
                      </InfoRow>
                      <InfoRow>
                        <InfoLabel>Next Billing</InfoLabel>
                        <InfoValue>{formatDate(subscription.nextBillingDate)}</InfoValue>
                      </InfoRow>
                    </div>

                    {/* All Available Plans */}
                    <div style={{ marginTop: '1rem' }}>
                      <div style={{ 
                        fontSize: '0.875rem', 
                        fontWeight: '600', 
                        color: '#718096',
                        marginBottom: '0.75rem',
                        paddingLeft: '0.5rem'
                      }}>
                        Available Plans:
                      </div>
                      {allPlans.map((plan) => {
                        const isActive = isCurrentPlan(plan.id);
                        return (
                          <DropdownItem 
                            key={plan.id}
                            style={{
                              background: isActive ? '#f0f9ff' : 'white',
                              border: isActive ? '2px solid #6BB8E8' : '1px solid #e2e8f0',
                              borderRadius: '8px',
                              padding: '1rem',
                              marginBottom: '0.75rem',
                              position: 'relative',
                            }}
                          >
                            {isActive && (
                              <div style={{
                                position: 'absolute',
                                top: '8px',
                                right: '8px',
                                background: '#6BB8E8',
                                color: 'white',
                                padding: '2px 8px',
                                borderRadius: '10px',
                                fontSize: '0.7rem',
                                fontWeight: '600',
                              }}>
                                Current
                              </div>
                            )}
                            
                            <div style={{ 
                              display: 'flex', 
                              justifyContent: 'space-between',
                              alignItems: 'flex-start',
                              marginBottom: '0.5rem',
                              paddingRight: isActive ? '70px' : '0'
                            }}>
                              <div>
                                <div style={{ 
                                  fontWeight: '700', 
                                  fontSize: '1rem',
                                  color: isActive ? '#6BB8E8' : '#2d3748',
                                  marginBottom: '4px'
                                }}>
                                  {plan.name}
                                </div>
                                <div style={{ 
                                  fontSize: '1.25rem', 
                                  fontWeight: '700',
                                  color: isActive ? '#6BB8E8' : '#1a202c'
                                }}>
                                  {formatPlanPrice(plan.priceCents, plan.currency)}
                                  <span style={{ 
                                    fontSize: '0.75rem', 
                                    fontWeight: '400', 
                                    color: '#718096' 
                                  }}>
                                    /{plan.period?.toLowerCase() || 'month'}
                                  </span>
                                </div>
                              </div>
                            </div>
                            
                            <p style={{ 
                              fontSize: '0.875rem', 
                              color: '#4a5568',
                              margin: 0,
                              lineHeight: '1.4'
                            }}>
                              {plan.description || 'No description available'}
                            </p>
                          </DropdownItem>
                        );
                      })}
                    </div>
                  </DropdownContent>
                )}
              </>
              ) : (
                // Subscription exists but plans not loaded yet
                <>
                  <DropdownButton onClick={() => setShowSubscriptionDetails(!showSubscriptionDetails)}>
                    <span>{subscription.planName || 'No Plan'}</span>
                    <span>{showSubscriptionDetails ? '▲' : '▼'}</span>
                  </DropdownButton>
                  
                  {showSubscriptionDetails && (
                    <DropdownContent>
                      <DropdownItem>
                        <InfoRow>
                          <InfoLabel>Status</InfoLabel>
                          <StatusBadge status={subscription.status}>
                            {subscription.status}
                          </StatusBadge>
                        </InfoRow>
                      </DropdownItem>
                      <DropdownItem>
                        <InfoRow>
                          <InfoLabel>Start Date</InfoLabel>
                          <InfoValue>{formatDate(subscription.startDate)}</InfoValue>
                        </InfoRow>
                      </DropdownItem>
                      <DropdownItem>
                        <InfoRow>
                          <InfoLabel>Next Billing</InfoLabel>
                          <InfoValue>{formatDate(subscription.nextBillingDate)}</InfoValue>
                        </InfoRow>
                      </DropdownItem>
                    </DropdownContent>
                  )}
                </>
              )
            ) : (
              <EmptyState>No active subscription</EmptyState>
            )}
          </CardContent>
        </Card>

        {/* SMS Balance Card */}
        <Card>
          <CardHeader>
            <CardTitle>
              SMS Balance
            </CardTitle>
          </CardHeader>
          <CardContent>
            {smsBalance ? (
              <>
                <InfoRow>
                  <InfoLabel>Remaining</InfoLabel>
                  <InfoValue style={{ color: '#6BB8E8', fontSize: '1.5rem' }}>
                    {smsBalance.remainingSmsCredits || 0}
                  </InfoValue>
                </InfoRow>
                <InfoRow>
                  <InfoLabel>Total Used</InfoLabel>
                  <InfoValue>{smsBalance.usedSmsCredits || 0} SMS</InfoValue>
                </InfoRow>
                <InfoRow>
                  <InfoLabel>Last Recharged</InfoLabel>
                  <InfoValue>{formatDate(smsBalance.lastRechargedAt)}</InfoValue>
                </InfoRow>
                <Link to={ROUTES.buySMS}>
                  <ViewButton style={{ width: '100%', marginTop: '1rem', textAlign: 'center', textDecoration: 'none' }}>
                    Buy More SMS
                  </ViewButton>
                </Link>
              </>
            ) : (
              <EmptyState>No SMS balance data</EmptyState>
            )}
          </CardContent>
        </Card>

        {/* Betalinger (Payment Methods) Card */}
        <LargeCard>
          <CardHeader>
            <CardTitle>
              Betalingsmetoder
            </CardTitle>
          </CardHeader>
          <CardContent>
            {paymentMethods && paymentMethods.length > 0 ? (
              <>
                {paymentMethods.map((method, index) => (
                  <PaymentItem key={index}>
                    <div>
                      <div style={{ fontWeight: 600, marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        {method.brand && (
                          <span style={{ textTransform: 'capitalize' }}>{method.brand}</span>
                        )}
                        <span>•••• {method.last4}</span>
                        {method.isDefault && (
                          <StatusBadge status="ACTIVE" style={{ fontSize: '0.65rem', padding: '2px 8px' }}>
                            Default
                          </StatusBadge>
                        )}
                      </div>
                      <PaymentDate>
                        Expires {method.expMonth}/{method.expYear}
                      </PaymentDate>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '4px' }}>
                      <StatusBadge status={method.status}>
                        {method.status}
                      </StatusBadge>
                      <span style={{ fontSize: '0.75rem', color: '#718096', textTransform: 'capitalize' }}>
                        {method.type}
                      </span>
                    </div>
                  </PaymentItem>
                ))}
                <Link to={ROUTES.paymentMethods}>
                  <ViewButton style={{ width: '100%', marginTop: '1rem', textAlign: 'center', textDecoration: 'none' }}>
                    Manage Payment Methods
                  </ViewButton>
                </Link>
              </>
            ) : (
              <>
                <EmptyState>No payment methods saved</EmptyState>
                <Link to={ROUTES.paymentMethods}>
                  <ViewButton style={{ width: '100%', marginTop: '1rem', textAlign: 'center', textDecoration: 'none' }}>
                    Add Payment Method
                  </ViewButton>
                </Link>
              </>
            )}
          </CardContent>
        </LargeCard>

        {/* Aktiviteter (Activities) Card */}
        <Card>
          <CardHeader>
            <CardTitle>
              Aktiviteter
            </CardTitle>
          </CardHeader>
          <CardContent>
            {activities && activities.length > 0 ? (
              activities.slice(0, 5).map((activity, index) => (
                <ActivityItem key={activity.id || index}>
                  <ActivityType>{formatActivityType(activity.type)}</ActivityType>
                  <ActivityDate>
                    {formatDate(activity.timestamp || activity.createdAt)}
                    {activity.status && ` • ${activity.status}`}
                  </ActivityDate>
                </ActivityItem>
              ))
            ) : (
              <EmptyState>No recent activities</EmptyState>
            )}
          </CardContent>
        </Card>

        {/* Kvitteringer (Receipts) Card */}
        <Card>
          <CardHeader>
            <CardTitle>
              Kvitteringer
            </CardTitle>
          </CardHeader>
          <CardContent>
            {receipts && receipts.length > 0 ? (
              receipts.slice(0, 5).map((receipt, index) => (
                <ReceiptItem key={index}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.875rem', marginBottom: '4px' }}>
                      Receipt #{receipt.id}
                    </div>
                    <PaymentDate>{formatDate(receipt.createdAt)}</PaymentDate>
                  </div>
                  <ViewButton>View</ViewButton>
                </ReceiptItem>
              ))
            ) : (
              <EmptyState>No receipts available</EmptyState>
            )}
          </CardContent>
        </Card>
      </DashboardGrid>
    </DashboardContainer>
  );
};

export default Dashboard;
